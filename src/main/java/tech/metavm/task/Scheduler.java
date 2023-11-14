package tech.metavm.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.entity.*;
import tech.metavm.util.Constants;
import tech.metavm.util.NncUtils;

import java.util.*;
import java.util.concurrent.*;

@Component
public class Scheduler {
    public static int THREAD_POOL_SIZE = 1;
    private final InstanceContextFactory instanceContextFactory;
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

    public Scheduler(InstanceContextFactory instanceContextFactory, TransactionOperations transactionOperations) {
        this.instanceContextFactory = instanceContextFactory;
        this.transactionOperations = transactionOperations;
        executor = new ThreadPoolExecutor(
                THREAD_POOL_SIZE, THREAD_POOL_SIZE, 0L, TimeUnit.SECONDS, new LinkedBlockingDeque<>()
        );
    }

    @Transactional
    public void createSchedulerStatus() {
        try (var rootContext = newRootContext()) {
            List<JobSchedulerStatus> existing = rootContext.getByType(JobSchedulerStatus.class, null, 1);
            if (NncUtils.isNotEmpty(existing)) {
                return;
            }
            rootContext.bind(new JobSchedulerStatus());
            rootContext.finish();
        }
    }

    @Scheduled(fixedDelay = 100)
    public void schedule() {
        if (!running) {
            return;
        }
        for (int i = 0; i < THREAD_POOL_SIZE; i++) {
            if (activeSignalMap.isEmpty() || !hasIdleWorkers()) {
                break;
            }
            executor.execute(this::scheduleOne);
        }
    }

    private void scheduleOne() {
        try(var rootContext = newRootContext()) {
            TaskSignal signal = selectSignalForScheduling(rootContext);
            if (signal != null) {
                if (signal.hasUnfinishedTasks()) {
                    runTaskForTenant(signal.getTenantId());
                } else {
                    removeSignal(signal);
                }
            }
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void pollSignals() {
        if (!running) {
            return;
        }
        long current = System.currentTimeMillis();
        try (var rootContext = newRootContext()) {
            List<TaskSignal> signals = rootContext.query(EntityIndexQuery.create(
                    TaskSignal.IDX_LAST_TASK_CREATED_AT,
                    List.of(
                            new EntityIndexQueryItem(
                                    TaskSignal.IDX_LAST_TASK_CREATED_AT.fieldName(0),
                                    lastSignalPollAt - 60000L // Account for time differences between machines
                            )
                    ),
                    IndexQueryOperator.GT,
                    512
            ));
            activeSignalMap.putAll(NncUtils.toMap(signals, TaskSignal::getTenantId));
            lastSignalPollAt = current;
        }
    }

    @Scheduled(fixedDelay = 500000000)
    public void sendHeartbeat() {
        long now = System.currentTimeMillis();
        try (var rootContext = newRootContext()) {
            JobSchedulerStatus schedulerStatus =
                    NncUtils.getFirst(rootContext.getByType(JobSchedulerStatus.class, null, 1));
            NncUtils.requireNonNull(schedulerStatus, "JobSchedulerStatus is not present");
            if (schedulerStatus.getVersion() == version || schedulerStatus.isHeartbeatTimeout()) {
                schedulerStatus.setLastHeartbeat(now);
                this.version = schedulerStatus.getVersion();
                rootContext.finish();
                running = true;
            } else {
                running = false;
            }
        }
    }

    private boolean hasIdleWorkers() {
        return executor.getActiveCount() < THREAD_POOL_SIZE;
    }

    private void runTaskForTenant(long tenantId) {
        List<Long> taskIds = NncUtils.requireNonNull(transactionOperations.execute(s -> takeTask(tenantId)));
        for (Long taskId : taskIds) {
            transactionOperations.executeWithoutResult(s -> runTask(tenantId, taskId));
        }
    }

    private List<Long> takeTask(long tenantId) {
        try (var context = newContext(tenantId)) {
            Objects.requireNonNull(context.getInstanceContext()).setLockMode(LockMode.EXCLUSIVE);
            List<Task> runnableTasks = context.query(
                    new EntityIndexQuery<>(
                            Task.IDX_STATE_LASTED_SCHEDULED_AT,
                            List.of(
                                    new EntityIndexQueryItem(
                                            Task.IDX_STATE_LASTED_SCHEDULED_AT.fieldName(0),
                                            TaskState.RUNNABLE
                                    ),
                                    new EntityIndexQueryItem(
                                            Task.IDX_STATE_LASTED_SCHEDULED_AT.fieldName(1),
                                            System.currentTimeMillis()
                                    )
                            ),
                            IndexQueryOperator.LE,
                            false,
                            10
                    )
            );
            List<Task> expiredRunningTasks = context.query(
                    new EntityIndexQuery<>(
                            Task.IDX_STATE_LASTED_SCHEDULED_AT,
                            List.of(
                                    new EntityIndexQueryItem(
                                            Task.IDX_STATE_LASTED_SCHEDULED_AT.fieldName(0),
                                            TaskState.RUNNING
                                    ),
                                    new EntityIndexQueryItem(
                                            Task.IDX_STATE_LASTED_SCHEDULED_AT.fieldName(1),
                                            System.currentTimeMillis() - 10000L
                                    )
                            ),
                            IndexQueryOperator.LE,
                            false,
                            10
                    )
            );
            List<Task> tasks = NncUtils.merge(List.of(runnableTasks, expiredRunningTasks));
            if (!tasks.isEmpty()) {
                for (Task task : tasks) {
                    task.setState(TaskState.RUNNING);
                    task.setLastRunAt(System.currentTimeMillis());
                }
                context.finish();
            }
            return NncUtils.map(tasks, Entity::getIdRequired);
        }
    }

    private void onTaskDone(long tenantId, long taskId) {
        TaskSignal signal = activeSignalMap.get(tenantId);
        if (signal != null) {
            try (var rootContext = newRootContext()) {
                signal = NncUtils.requireNonNull(rootContext.selectByUniqueKey(TaskSignal.IDX_TENANT_ID, tenantId));
                if (signal.decreaseUnfinishedJobCount()) {
                    removeSignal(signal);
                } else {
                    addSignal(signal);
                }
                rootContext.finish();
            }
        }
        notifyWaitingThreads(taskId);
    }

    private void notifyWaitingThreads(long taskId) {
        NncUtils.afterCommit(() -> {
            Object monitor = monitorMap.get(taskId);
            if (monitor != null) {
                synchronized (monitor) {
                    monitor.notifyAll();
                }
                monitorMap.remove(taskId);
            }
        });
    }

    private void runTask(long tenantId, long taskId) {
        if (!runningTaskIds.add(taskId)) {
            return;
        }
        try {
            try(var context = newContext(tenantId)) {
                Objects.requireNonNull(context.getInstanceContext()).setLockMode(LockMode.EXCLUSIVE);
                Task task = context.getEntity(Task.class, taskId);
                if (task.isRunning()) {
                    task.run(context.getInstanceContext());
                    if (task.isFinished()) {
                        tryRemoveTask(task, context);
                    }
                    context.finish();
                    if (task.isFinished()) {
                        onTaskDone(tenantId, taskId);
                    }
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
        if (group == null) {
            context.remove(task);
        } else if (group.isDone()) {
            context.remove(group);
        }
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
            signal = rootEntityCtx.getEntity(TaskSignal.class, signal.getIdRequired());
            addSignal(signal);
        }
        return lastScheduledSignal = signal;
    }

    private TaskSignal nextSignal(TaskSignal signal) {
        if (signal == null) {
            return NncUtils.get(activeSignalMap.firstEntry(), Map.Entry::getValue);
        }
        TaskSignal next = NncUtils.get(activeSignalMap.higherEntry(signal.getTenantId()), Map.Entry::getValue);
        if (next == null) {
            next = NncUtils.get(activeSignalMap.firstEntry(), Map.Entry::getValue);
        }
        return next;
    }


    private void addSignal(TaskSignal signal) {
        activeSignalMap.put(signal.getTenantId(), signal);
    }

    private void removeSignal(TaskSignal signal) {
        activeSignalMap.remove(signal.getTenantId());
    }

    private IEntityContext newRootContext() {
        return instanceContextFactory.newBuilder()
                .tenantId(-1L)
                .asyncLogProcessing(true)
                .build();
    }

    private IEntityContext newContext(long tenantId) {
        return instanceContextFactory.newBuilder()
                .tenantId(tenantId)
                .asyncLogProcessing(true)
                .build();
    }

    public void waitForJobDone(Task task, int maxSchedules) {
        NncUtils.requireNonNull(task.getId());
        Object monitor = monitorMap.computeIfAbsent(task.getId(), k -> new Object());
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
