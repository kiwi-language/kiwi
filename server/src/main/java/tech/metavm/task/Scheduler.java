package tech.metavm.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.entity.*;
import tech.metavm.util.NncUtils;
import tech.metavm.util.TransactionUtils;

import java.util.*;
import java.util.concurrent.*;

@Component
public class Scheduler extends EntityContextFactoryBean {
    public static int THREAD_POOL_SIZE = 1;
    private final NavigableMap<Long, TaskSignal> activeSignalMap = new ConcurrentSkipListMap<>();
    private final TransactionOperations transactionOperations;
    private TaskSignal lastScheduledSignal;
    private long lastSignalPollAt;
    private long version;
    private volatile boolean running;
    private final Map<Long, Object> monitorMap = new ConcurrentSkipListMap<>();
    private final ThreadPoolExecutor executor;
    private final Executor scheduleExecutor = Executors.newSingleThreadExecutor(
            r -> new Thread(r, "scheduler-worker-1")
    );

    private final Set<Long> runningTaskIds = new ConcurrentSkipListSet<>();

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
            List<JobSchedulerStatus> existing = platformContext.getByType(JobSchedulerStatus.class, null, 1);
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
                            .addGtItem(0, lastSignalPollAt - 60000L)
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
                    NncUtils.first(platformContext.getByType(JobSchedulerStatus.class, null, 1));
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
        List<Long> taskIds = NncUtils.requireNonNull(transactionOperations.execute(s -> takeTask(appId)));
        for (Long taskId : taskIds) {
            transactionOperations.executeWithoutResult(s -> runTask(appId, taskId));
        }
    }

    private List<Long> takeTask(long appId) {
        try (var context = newContext(appId)) {
            Objects.requireNonNull(context.getInstanceContext()).setLockMode(LockMode.EXCLUSIVE);
            List<Task> runnableTasks = context.query(
                    Task.IDX_STATE_LAST_RUN_AT.newQueryBuilder()
                            .addEqItem(0, TaskState.RUNNABLE)
                            .addLeItem(1, System.currentTimeMillis())
                            .limit(10)
                            .build()
            );
            List<Task> expiredRunningTasks = context.query(
                    Task.IDX_STATE_LAST_RUN_AT.newQueryBuilder()
                            .addEqItem(0, TaskState.RUNNING)
                            .addLeItem(1, System.currentTimeMillis() - 10000L)
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
            return NncUtils.map(tasks, Entity::tryGetId);
        }
    }

    private void onTaskDone(long appId, long taskId) {
        TaskSignal signal = activeSignalMap.get(appId);
        if (signal != null) {
            try (var platformContext = newPlatformContext()) {
                signal = NncUtils.requireNonNull(platformContext.selectByUniqueKey(TaskSignal.IDX_APP_ID, appId));
                if (signal.decreaseUnfinishedJobCount())
                    removeSignal(signal);
                else
                    addSignal(signal);
                platformContext.finish();
            }
        }
        notifyWaitingThreads(taskId);
    }

    private void notifyWaitingThreads(long taskId) {
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

    private void runTask(long appId, long taskId) {
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
        if (task instanceof ReferenceCleanupTask) {
            return;
        }
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
            signal = rootEntityCtx.getEntity(TaskSignal.class, signal.tryGetId());
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
        Object monitor = monitorMap.computeIfAbsent(task.tryGetId(), k -> new Object());
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
