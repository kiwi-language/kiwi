package org.metavm.task;

import org.metavm.entity.*;
import org.metavm.util.InternalException;
import org.metavm.util.NetworkUtils;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionOperations;

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

    public Worker(EntityContextFactory entityContextFactory, TransactionOperations transactionOperations, TaskRunner taskRunner) {
        super(entityContextFactory);
        this.transactionOperations = transactionOperations;
        this.taskRunner = taskRunner;
    }

    @Scheduled(fixedDelay = 10000)
    public void sendHeartbeat() {
        transactionOperations.executeWithoutResult(s -> {
            try (var context = newPlatformContext()) {
                var executorData = context.selectFirstByKey(ExecutorData.IDX_IP, NetworkUtils.localIP);
                if (executorData == null) {
                    executorData = new ExecutorData(NetworkUtils.localIP);
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

    public boolean waitFor(Predicate<Task> predicate, int maxRuns, long delay) {
        for (int i = 0; i < maxRuns; i++) {
            var tasks = run0();
            if (NncUtils.anyMatch(tasks, t -> t.isCompleted() && predicate.test(t)))
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
            if (NncUtils.anyMatch(tasks, t -> t.getGroup() != null && t.getGroup().isCompleted() && predicate.test(t.getGroup())))
                return true;
        }
        return false;
    }

    public List<Task> run0() {
        try (var context = newPlatformContext()) {
            var tasks = context.query(
                    new EntityIndexQuery<>(
                            ShadowTask.IDX_EXECUTOR_IP_START_AT,
                            new EntityIndexKey(List.of(NetworkUtils.localIP, 0)),
                            new EntityIndexKey(List.of(NetworkUtils.localIP, System.currentTimeMillis())),
                            false,
                            16
                    )
            );
            var futures = new ArrayList<Future<Task>>();
            tasks.forEach(t -> futures.add(taskRunner.run(() -> runTask(t))));
            var appTasks = new ArrayList<Task>();
            futures.forEach(f -> {
                try {
                    appTasks.add(f.get());
                } catch (InterruptedException e) {
                    throw new InternalException("Should not happen", e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
            return appTasks;
        }
    }

    private Task runTask(ShadowTask shadowTask) {
        return transactionOperations.execute(s -> {
            try (var appContext = newContext(shadowTask.getAppId())) {
                var appTask = appContext.getEntity(Task.class, shadowTask.getAppTaskId());
                appContext.getInstanceContext().setTimeout(appTask.getTimeout());
                logger.info("Running task {}", EntityUtils.getRealType(appTask).getSimpleName());
                boolean terminated;
                try {
                    if (appTask instanceof WalTask walTask) {
                        try (var walContext = newContext(shadowTask.getAppId(),
                                builder -> builder.readWAL(walTask.getWAL())
                                        .migrationDisabled(walTask.isMigrationDisabled())
                                        .timeout(appTask.getTimeout()))) {
                            terminated = runTask0(appTask, walContext, appContext);
                            if(!appTask.isFailed())
                                walContext.finish();
                        }
                    } else {
                        try (var executionContext = newContext(shadowTask.getAppId())) {
                            terminated = runTask0(appTask, executionContext, appContext);
                            if(!appTask.isFailed())
                                executionContext.finish();
                        }
                    }
                }
                catch (Exception e) {
                    logger.error("Failed to execute task {}", appTask.getTitle(), e);
                    terminated = true;
                }
                if (terminated) {
                    var group = appTask.getGroup();
                    if (group != null) {
                        if (group.isTerminated())
                            appContext.remove(group);
                    } else
                        appContext.remove(appTask);
                    try (var context = newPlatformContext()) {
                        context.remove(context.getEntity(ShadowTask.class, shadowTask.getId()));
                        context.finish();
                    }
                }
                appContext.finish();
                return appTask;
            }
        });
    }

    private boolean runTask0(Task appTask, IEntityContext executionContext, IEntityContext taskContext) {
        appTask.run(executionContext, taskContext);
        return appTask.isTerminated();
    }

}
