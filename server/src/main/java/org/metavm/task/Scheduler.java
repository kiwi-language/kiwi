package org.metavm.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import org.metavm.entity.*;
import org.metavm.util.NncUtils;
import org.metavm.util.TransactionUtils;

import java.util.*;
import java.util.concurrent.*;

@Component
public class Scheduler extends EntityContextFactoryAware {
    public static int THREAD_POOL_SIZE = 1;
    private final NavigableMap<Long, TaskSignal> activeSignalMap = new ConcurrentSkipListMap<>();
    private final TransactionOperations transactionOperations;
    private TaskSignal lastScheduledSignal;
    private long lastSignalPollAt;
    private long version;
    private volatile boolean running;
    private final Map<String, Object> monitorMap = new ConcurrentSkipListMap<>();
    private final ThreadPoolExecutor executor;
    private final Executor scheduleExecutor = Executors.newSingleThreadExecutor(
            r -> new Thread(r, "scheduler-worker-1")
    );

    private final Set<String> runningTaskIds = new ConcurrentSkipListSet<>();

    public Scheduler(EntityContextFactory entityContextFactory, TransactionOperations transactionOperations) {
        super(entityContextFactory);
        this.transactionOperations = transactionOperations;
        executor = new ThreadPoolExecutor(
                THREAD_POOL_SIZE, THREAD_POOL_SIZE, 0L, TimeUnit.SECONDS, new LinkedBlockingDeque<>()
        );
    }

    @Transactional
    public void createSchedulerStatus() {
        try (var platformContext = newPlatformContext()) {
            List<JobSchedulerStatus> existing = platformContext.selectByKey(JobSchedulerStatus.IDX_ALL_FLAG, true);
            if (NncUtils.isNotEmpty(existing))
                return;
            platformContext.bind(new JobSchedulerStatus());
            platformContext.finish();
        }
    }

    @Scheduled(fixedDelay = 100)
    public void schedule() {
        if (!running) {
            return;
        }
        for (int i = 0; i < THREAD_POOL_SIZE; i++) {
            if (activeSignalMap.isEmpty() || !hasIdleWorkers())
                break;
            executor.execute(this::scheduleOne);
        }
    }

    private void scheduleOne() {
        try (var platformContext = newPlatformContext()) {
            TaskSignal signal = selectSignalForScheduling(platformContext);
            if (signal != null) {
                if (signal.hasUnfinishedTasks())
                    runTaskForApplication(signal.getAppId());
                else
                    removeSignal(signal);
            }
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void pollSignals() {
        if (!running)
            return;
        long current = System.currentTimeMillis();
        try (var platformContext = newPlatformContext()) {
            List<TaskSignal> signals = platformContext.query(
                    TaskSignal.IDX_LAST_TASK_CREATED_AT.newQueryBuilder()
                            .from(new EntityIndexKey(List.of(lastSignalPollAt - 60000L)))
                            .limit(512)
                            .build()
            );
            activeSignalMap.putAll(NncUtils.toMap(signals, TaskSignal::getAppId));
            lastSignalPollAt = current;
        }
    }

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void sendHeartbeat() {
        long now = System.currentTimeMillis();
        try (var platformContext = newPlatformContext()) {
            JobSchedulerStatus schedulerStatus =
                    platformContext.selectFirstByKey(JobSchedulerStatus.IDX_ALL_FLAG, true);
            NncUtils.requireNonNull(schedulerStatus, "JobSchedulerStatus is not present");
            if (schedulerStatus.getVersion() == version || schedulerStatus.isHeartbeatTimeout()) {
                schedulerStatus.setLastHeartbeat(now);
                this.version = schedulerStatus.getVersion();
                platformContext.finish();
                running = true;
            } else
                running = false;
        }
    }

    private boolean hasIdleWorkers() {
        return executor.getActiveCount() < THREAD_POOL_SIZE;
    }

    private void runTaskForApplication(long appId) {
        var taskIds = NncUtils.requireNonNull(transactionOperations.execute(s -> takeTask(appId)));
        for (var taskId : taskIds) {
            transactionOperations.executeWithoutResult(s -> runTask(appId, taskId));
        }
    }

    private List<String> takeTask(long appId) {
        try (var context = newContext(appId)) {
            Objects.requireNonNull(context.getInstanceContext()).setLockMode(LockMode.EXCLUSIVE);
            List<Task> runnableTasks = context.query(
                    Task.IDX_STATE_LAST_RUN_AT.newQueryBuilder()
                            .from(new EntityIndexKey(List.of(TaskState.RUNNABLE, 0L)))
                            .to(new EntityIndexKey(List.of(TaskState.RUNNABLE, System.currentTimeMillis())))
                            .limit(10)
                            .build()
            );
            List<Task> expiredRunningTasks = context.query(
                    Task.IDX_STATE_LAST_RUN_AT.newQueryBuilder()
                            .from(new EntityIndexKey(List.of(TaskState.RUNNING, 0L)))
                            .to(new EntityIndexKey(List.of(TaskState.RUNNING, System.currentTimeMillis() - 10000L)))
                            .limit(10)
                            .build()
            );
            List<Task> tasks = NncUtils.merge(List.of(runnableTasks, expiredRunningTasks));
            if (!tasks.isEmpty()) {
                for (Task task : tasks) {
                    task.setState(TaskState.RUNNING);
                    task.setLastRunAt(System.currentTimeMillis());
                }
                context.finish();
            }
            return NncUtils.map(tasks, Entity::getStringId);
        }
    }

    private void onTaskDone(long appId, String taskId) {
        TaskSignal signal = activeSignalMap.get(appId);
        if (signal != null) {
            try (var platformContext = newPlatformContext()) {
                signal = NncUtils.requireNonNull(platformContext.selectFirstByKey(TaskSignal.IDX_APP_ID, appId));
                if (signal.decreaseUnfinishedJobCount())
                    removeSignal(signal);
                else
                    addSignal(signal);
                platformContext.finish();
            }
        }
        notifyWaitingThreads(taskId);
    }

    private void notifyWaitingThreads(String taskId) {
        TransactionUtils.afterCommit(() -> {
            Object monitor = monitorMap.get(taskId);
            if (monitor != null) {
                synchronized (monitor) {
                    monitor.notifyAll();
                }
                monitorMap.remove(taskId);
            }
        });
    }

    private void runTask(long appId, String taskId) {
        if (!runningTaskIds.add(taskId))
            return;
        try {
            try (var context = newContext(appId)) {
                Objects.requireNonNull(context.getInstanceContext()).setLockMode(LockMode.EXCLUSIVE);
                Task task = context.getEntity(Task.class, taskId);
                if (task.isRunning()) {
                    task.run(context);
                    if (task.isFinished())
                        tryRemoveTask(task, context);
                    context.finish();
                    if (task.isFinished())
                        onTaskDone(appId, taskId);
                }
            }
        } finally {
            runningTaskIds.remove(taskId);
        }
    }

    private void tryRemoveTask(Task task, IEntityContext context) {
        if (task instanceof ReferenceCleanupTask)
            return;
        NncUtils.requireTrue(task.isFinished());
        var group = task.getGroup();
        if (group == null)
            context.remove(task);
        else if (group.isDone())
            context.remove(group);
    }

    private TaskSignal selectSignalForScheduling(IEntityContext rootEntityCtx) {
        TaskSignal start = nextSignal(lastScheduledSignal);
        TaskSignal signal = start;
        while (signal != null && !signal.hasUnfinishedTasks()) {
            signal = nextSignal(signal);
            if (signal == start) {
                signal = null;
                break;
            }
        }
        if (signal != null) {
            signal = rootEntityCtx.getEntity(TaskSignal.class, signal.getId());
            addSignal(signal);
        }
        return lastScheduledSignal = signal;
    }

    private TaskSignal nextSignal(TaskSignal signal) {
        if (signal == null)
            return NncUtils.get(activeSignalMap.firstEntry(), Map.Entry::getValue);
        TaskSignal next = NncUtils.get(activeSignalMap.higherEntry(signal.getAppId()), Map.Entry::getValue);
        if (next == null)
            next = NncUtils.get(activeSignalMap.firstEntry(), Map.Entry::getValue);
        return next;
    }


    private void addSignal(TaskSignal signal) {
        activeSignalMap.put(signal.getAppId(), signal);
    }

    private void removeSignal(TaskSignal signal) {
        activeSignalMap.remove(signal.getAppId());
    }

    public void waitForJobDone(Task task, int maxSchedules) {
        NncUtils.requireNonNull(task.tryGetId());
        Object monitor = monitorMap.computeIfAbsent(task.getStringId(), k -> new Object());
        scheduleExecutor.execute(() -> {
            for (int i = 0; i < maxSchedules; i++) {
                schedule();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignore) {
                }
            }
        });
        synchronized (monitor) {
            try {
                monitor.wait();
            } catch (InterruptedException ignored) {
            }
        }
    }

}
