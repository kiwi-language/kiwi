package tech.metavm.job;

import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.entity.*;
import tech.metavm.util.Constants;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.concurrent.*;

import static tech.metavm.job.Job.IDX_STATE_LASTED_SCHEDULED_AT;
import static tech.metavm.job.JobSignal.IDX_LAST_JOB_CREATED_AT;

@Component
public class JobScheduler {
    public static int THREAD_POOL_SIZE = 1;
    private final InstanceContextFactory instanceContextFactory;
    private final NavigableMap<Long, JobSignal> activeSignalMap = new ConcurrentSkipListMap<>();
    private final TransactionOperations transactionOperations;
    private JobSignal lastScheduledSignal;
    private long lastSignalPollAt;
    private long version;
    private volatile boolean running;
    private final Map<Long, Object> monitorMap = new ConcurrentSkipListMap<>();
    private final ThreadPoolExecutor executor;
    private final Executor scheduleExecutor = Executors.newSingleThreadExecutor(
            r -> new Thread(r, "scheduler-worker-1")
    );

    private final Set<Long> runningJobIds = new ConcurrentSkipListSet<>();

    public JobScheduler(InstanceContextFactory instanceContextFactory, TransactionOperations transactionOperations) {
        this.instanceContextFactory = instanceContextFactory;
        this.transactionOperations = transactionOperations;
        executor = new ThreadPoolExecutor(
                THREAD_POOL_SIZE, THREAD_POOL_SIZE, 0L, TimeUnit.SECONDS, new LinkedBlockingDeque<>()
        );
    }

    @Transactional
    public void createSchedulerStatus() {
        IEntityContext rootContext = newRootContext();
        List<JobSchedulerStatus> existing = rootContext.getByType(JobSchedulerStatus.class, null, 1);
        if(NncUtils.isNotEmpty(existing)) {
            return;
        }
        rootContext.bind(new JobSchedulerStatus());
        rootContext.finish();
    }

    @Scheduled(fixedDelay = 100)
    public void schedule() {
        if(!running) {
            return;
        }
        for (int i = 0; i < THREAD_POOL_SIZE; i++) {
            if(activeSignalMap.isEmpty() || !hasIdleWorkers()) {
                break;
            }
            transactionOperations.executeWithoutResult(s -> scheduleOne());
        }
    }

    private void scheduleOne() {
        IEntityContext rootContext = newRootContext();
        JobSignal signal = selectSignalForScheduling(rootContext);
        if(signal != null) {
            if(signal.hasUnfinishedJobs()) {
                runJobForTenant(signal.getTenantId());
            }
            else {
                removeSignal(signal);
            }
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void pollSignals() {
        if(!running) {
            return;
        }
        long current = System.currentTimeMillis();
        IEntityContext rootContext = newRootContext();
        List<JobSignal> signals = rootContext.query(EntityIndexQuery.create(
                IDX_LAST_JOB_CREATED_AT,
                List.of(
                        new EntityIndexQueryItem(
                                IDX_LAST_JOB_CREATED_AT.fieldName(0),
                                lastSignalPollAt - 60000L // Account for time differences between machines
                        )
                ),
                IndexQueryOperator.GT,
                512
        ));
        activeSignalMap.putAll(NncUtils.toMap(signals, JobSignal::getTenantId));
        lastSignalPollAt = current;
    }

    @Scheduled(fixedDelay = 5000)
    public void sendHeartbeat() {
        long now = System.currentTimeMillis();
        IEntityContext rootContext = newRootContext();
        JobSchedulerStatus schedulerStatus =
                NncUtils.getFirst(rootContext.getByType(JobSchedulerStatus.class, null, 1));
        NncUtils.requireNonNull(schedulerStatus, "JobSchedulerStatus is not present");
        if(schedulerStatus.getVersion() == version || schedulerStatus.isHeartbeatTimeout()) {
            schedulerStatus.setLastHeartbeat(now);
            this.version = schedulerStatus.getVersion();
            rootContext.finish();
            running = true;
        }
        else {
            running = false;
        }
    }

    private boolean hasIdleWorkers() {
        return executor.getActiveCount() < THREAD_POOL_SIZE;
    }

    private void runJobForTenant(long tenantId) {
        IEntityContext context = newContext(tenantId);
        List<Job> jobs = context.query(
                new EntityIndexQuery<>(
                        IDX_STATE_LASTED_SCHEDULED_AT,
                        List.of(
                                new EntityIndexQueryItem(
                                        IDX_STATE_LASTED_SCHEDULED_AT.fieldName(0),
                                        JobState.RUNNABLE
                                ),
                                new EntityIndexQueryItem(
                                        IDX_STATE_LASTED_SCHEDULED_AT.fieldName(1),
                                        System.currentTimeMillis()
                                )
                        ),
                        IndexQueryOperator.LE,
                        true,
                        1
                )
        );
        if(NncUtils.isEmpty(jobs)) {
            return;
        }
        Job job = jobs.get(0);
        job.setLastRunAt(System.currentTimeMillis());
        context.finish();
        executor.execute(() ->
            transactionOperations.executeWithoutResult(s -> runJob(tenantId, job.getId()))
        );
    }

    private void onJobDone(long tenantId, long jobId) {
        JobSignal signal = activeSignalMap.get(tenantId);
        if(signal != null) {
            IEntityContext rootContext = newRootContext();
            signal = rootContext.selectByUniqueKey(JobSignal.IDX_TENANT_ID, tenantId);
            if (signal.decreaseUnfinishedJobCount()) {
                removeSignal(signal);
            }
            else {
                addSignal(signal);
            }
            rootContext.finish();
        }
        notifyWaitingThreads(jobId);
    }

    private void notifyWaitingThreads(long jobId) {
        NncUtils.afterCommit(() -> {
            Object monitor = monitorMap.get(jobId);
            if(monitor != null) {
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (monitor) {
                    monitor.notifyAll();
                }
                monitorMap.remove(jobId);
            }
        });
    }

    private void runJob(long tenantId, long jobId) {
        if(!runningJobIds.add(jobId)) {
            return;
        }
        try {
            IEntityContext context = newContext(tenantId);
            Job job = context.getEntity(Job.class, jobId);
            if (job.isRunnable()) {
                job.run(context.getInstanceContext());
                context.finish();
                if (job.isFinished()) {
                    onJobDone(tenantId, jobId);
                }
            }
        }
        finally {
            runningJobIds.remove(jobId);
        }
    }

    private JobSignal selectSignalForScheduling(IEntityContext rootEntityCtx) {
        JobSignal start = nextSignal(lastScheduledSignal);
        JobSignal signal = start;
        while (signal != null && !signal.hasUnfinishedJobs()) {
            signal = nextSignal(signal);
            if(signal == start) {
                signal = null;
                break;
            }
        }
        if(signal != null) {
            signal = rootEntityCtx.getEntity(JobSignal.class, signal.getId());
            addSignal(signal);
        }
        return lastScheduledSignal = signal;
    }

    private JobSignal nextSignal(JobSignal signal) {
        if(signal == null) {
            return NncUtils.get(activeSignalMap.firstEntry(), Map.Entry::getValue);
        }
        JobSignal next = NncUtils.get(activeSignalMap.higherEntry(signal.getTenantId()), Map.Entry::getValue);
        if(next == null) {
            next = NncUtils.get(activeSignalMap.firstEntry(), Map.Entry::getValue);
        }
        return next;
    }


    private void addSignal(JobSignal signal) {
        activeSignalMap.put(signal.getTenantId(), signal);
    }

    private void removeSignal(JobSignal signal) {
        activeSignalMap.remove(signal.getTenantId());
    }

    private IEntityContext newRootContext() {
        return instanceContextFactory.newContext(Constants.ROOT_TENANT_ID).getEntityContext();
    }

    private IEntityContext newContext(long tenantId) {
        return instanceContextFactory.newContext(tenantId).getEntityContext();
    }

    public void waitForJobDone(Job job, int maxSchedules) {
        NncUtils.requireNonNull(job.getId());
        Object monitor = monitorMap.computeIfAbsent(job.getId(), k -> new Object());
        scheduleExecutor.execute(() -> {
            for (int i = 0; i < maxSchedules; i++) {
                schedule();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignore) {
                }
            }
        });
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (monitor) {
            try {
                monitor.wait();
            } catch (InterruptedException ignored) {
            }
        }
    }

}
