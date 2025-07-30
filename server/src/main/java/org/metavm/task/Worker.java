package org.metavm.task;

//import org.metavm.ddl.DefContextUtils;

import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionOperations;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Predicate;

@Component
public class Worker extends EntityContextFactoryAware {

    public static final Logger logger = LoggerFactory.getLogger(Worker.class);

    private final TransactionOperations transactionOperations;
    private final TaskRunner taskRunner;
    private final MetaContextCache metaContextCache;

    public Worker(EntityContextFactory entityContextFactory, TransactionOperations transactionOperations, TaskRunner taskRunner, MetaContextCache metaContextCache) {
        super(entityContextFactory);
        this.transactionOperations = transactionOperations;
        this.taskRunner = taskRunner;
        this.metaContextCache = metaContextCache;
    }

    @Scheduled(fixedDelay = 10000)
    public void sendHeartbeat() {
        transactionOperations.executeWithoutResult(s -> {
            try (var context = newPlatformContext()) {
                var executorData = context.selectFirstByKey(ExecutorData.IDX_IP, Instances.stringInstance(NetworkUtils.localIP));
                if (executorData == null) {
                    executorData = new ExecutorData(context.allocateRootId(), NetworkUtils.localIP);
                    context.bind(executorData);
                }
                executorData.setAvailable(true);
                executorData.setLastHeartbeat(System.currentTimeMillis());
                context.finish();
            }
        });
    }

    @Scheduled(fixedDelay = 100)
    public void run() {
        run0();
    }

    public void waitForAllDone() {
        while (true) {
            var tasks = run0();
            if(tasks.isEmpty())
                return;
        }
    }

    public boolean waitFor(Predicate<Task> predicate, int maxRuns, long delay) {
        for (int i = 0; i < maxRuns; i++) {
            var tasks = run0();
            if (Utils.anyMatch(tasks, t -> t.isCompleted() && predicate.test(t)))
                return true;
            if(delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return false;
    }

    public boolean waitForGroup(Predicate<TaskGroup> predicate, int maxRuns) {
        for (int i = 0; i < maxRuns; i++) {
            var tasks = run0();
            if (Utils.anyMatch(tasks, t -> t.getGroup() != null && t.getGroup().isCompleted() && predicate.test(t.getGroup())))
                return true;
        }
        return false;
    }

    public List<Task> run0() {
        try (var context = newPlatformContext()) {
            var tasks = context.query(
                    new EntityIndexQuery<>(
                            ShadowTask.IDX_EXECUTOR_IP_START_AT,
                            new EntityIndexKey(List.of(
                                    Instances.stringInstance(NetworkUtils.localIP),
                                    Instances.longInstance(0)
                                    )),
                            new EntityIndexKey(List.of(
                                    Instances.stringInstance(NetworkUtils.localIP),
                                    Instances.longInstance(System.currentTimeMillis()))
                            ),
                            false,
                            16
                    )
            );
            var futures = new ArrayList<Future<Task>>();
            tasks.forEach(t -> futures.add(taskRunner.run(() -> runTask(t))));
            var appTasks = new ArrayList<Task>();
            futures.forEach(f -> {
                try {
                    var t = f.get();
                    if (t != null)
                        appTasks.add(t);
                } catch (InterruptedException e) {
                    throw new InternalException("Should not happen", e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
            return appTasks;
        }
    }

    private @Nullable Task runTask(ShadowTask shadowTask) {
        var tracing = DebugEnv.traceTaskExecution;
        return transactionOperations.execute(s -> {
            if (isAppRemoved(shadowTask.getAppId())) {
                removeShadowTask(shadowTask.getId());
                return null;
            }
            try (var taskCtx = newContext(shadowTask.getAppId())) {
                var appTask = taskCtx.getEntity(Task.class, shadowTask.getAppTaskId());
                taskCtx.setTimeout(appTask.getTimeout());
                boolean terminated;
                try {
                    if (appTask.isMigrating())
                        ContextUtil.setDDL(true);
                    var parentContext = shadowTask.getAppId() != Constants.ROOT_APP_ID ?
                                metaContextCache.get(shadowTask.getAppId(), appTask.isMigrating()/*Utils.safeCall(appTask.getMetaWAL(), Instance::getId)*/) :
                                            ModelDefRegistry.getDefContext();
                    try (var exeCtx = entityContextFactory.newContext(shadowTask.getAppId(), parentContext,
                            builder -> builder
                                    .relocationEnabled(appTask.isRelocationEnabled())
                                    .migrating(appTask.isMigrating())
                                    .timeout(appTask.getTimeout()))) {
                        terminated = runTask0(appTask, exeCtx, taskCtx);
                        if(!appTask.isFailed())
                            exeCtx.finish();
                        if(terminated)
                            logger.info("Task {}-{} completed successfully", shadowTask.getAppId(), appTask.getTitle());
                    }
                }
                catch (Exception e) {
                    logger.error("Failed to execute task {}-{}", shadowTask.getAppId(), appTask.getTitle(), e);
                    terminated = true;
                }
                finally {
                    if (appTask.isMigrating())
                        ContextUtil.setDDL(false);
                }
                if (terminated) {
                    removeShadowTask(shadowTask.getId());
                }
                if (tracing)
                    logger.trace("After running task {}. task ID: {}, terminated: {}",
                            appTask.getTitle(), appTask.getId(), terminated);
                taskCtx.finish();
                return appTask;
            }
        });
    }

    private boolean isAppRemoved(long appId) {
        try (var context = newPlatformContext()) {
            try {
                context.get(PhysicalId.of(appId, 0));
                return false;
            } catch (BusinessException e) {
                if (e.getErrorCode() == ErrorCode.INSTANCE_NOT_FOUND)
                    return true;
                throw e;
            }
        }
    }

    private void removeShadowTask(Id id) {
        if (DebugEnv.traceTaskExecution)
            logger.trace("Removing shadow task {}", id);
        try (var context = newPlatformContext()) {
            try {
                context.remove(context.getEntity(ShadowTask.class, id));
            } catch (Exception e) {
                logger.error("Failed to remove shadow task", e);
            }
            context.finish();
        }
    }

    private boolean runTask0(Task appTask, IInstanceContext executionContext, IInstanceContext taskContext) {
        appTask.run(executionContext, taskContext);
        return appTask.isTerminated();
    }

}
