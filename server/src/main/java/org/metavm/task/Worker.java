package org.metavm.task;

import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.entity.EntityUtils;
import org.metavm.entity.IEntityContext;
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

    private static final int MAX_RUNS = 10;

    public void waitFor(Predicate<Task> predicate) {
        for (int i = 0; i < MAX_RUNS; i++) {
            var tasks = run0();
            if (NncUtils.anyMatch(tasks, t -> t.isFinished() && predicate.test(t)))
                return;
        }
        throw new IllegalStateException("Condition not met after " + MAX_RUNS + " runs");
    }

    public List<Task> run0() {
        try (var context = newPlatformContext()) {
            var tasks = context.selectByKey(ShadowTask.IDX_EXECUTOR_IP, NetworkUtils.localIP);
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
                logger.info("Running task {}", EntityUtils.getRealType(appTask).getSimpleName());
                boolean done;
                if (appTask instanceof WalTask walTask) {
                    try (var walContext = entityContextFactory.newLoadedContext(shadowTask.getAppId(), walTask.getWAL(), walTask.isMigrationDisabled())) {
                        done = runTask0(appTask, walContext);
                        walContext.finish();
                    }
                } else
                    done = runTask0(appTask, appContext);
                if (done) {
                    var group = appTask.getGroup();
                    if (group != null) {
                        if (group.isDone())
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

    private boolean runTask0(Task appTask, IEntityContext appContext) {
        appTask.run(appContext);
        return appTask.isFinished();
    }

}
