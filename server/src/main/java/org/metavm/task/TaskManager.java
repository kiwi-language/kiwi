package org.metavm.task;

import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.WAL;
import org.metavm.util.Constants;
import org.metavm.util.ContextUtil;
import org.metavm.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component
public class TaskManager extends EntityContextFactoryAware {

    public static final Logger logger = LoggerFactory.getLogger(TaskManager.class);

    private final TransactionOperations transactionTemplate;

    public TaskManager(EntityContextFactory entityContextFactory, TransactionOperations transactionTemplate) {
        super(entityContextFactory);
        this.transactionTemplate = transactionTemplate;
        Executor executor = Executors.newSingleThreadExecutor();
        ShadowTask.saveShadowTasksHook = this::createShadowTasks;
//        executor.execute(() -> {
//            //noinspection InfiniteLoopStatement
//            while (true) {
//                executeJob();
//                //noinspection CatchMayIgnoreException
//                try {
//                    //noinspection BusyWait
//                    Thread.sleep(1000L);
//                } catch (InterruptedException e) {
//                }
//            }
//        });
    }


    @Transactional
    public void addGlobalTask(Task task) {
        try (IEntityContext context = newPlatformContext()) {
            context.bind(task);
            context.finish();
        }
    }

    @Transactional
    public void runTask(String taskClassName) {
        try (IEntityContext context = newContext()) {
            Class<? extends Task> taskClass = ReflectionUtils.classForName(taskClassName).asSubclass(Task.class);
        }
    }

    @Transactional
    public void createShadowTasks(long appId, List<Task> created) {
        try (var platformContext = entityContextFactory.newContext(Constants.PLATFORM_APP_ID, builder -> builder.skipPostProcessing(true));
        var ignored = ContextUtil.getProfiler().enter("createShadowTasks")) {
            platformContext.setDescription("ShadowTask");
            for (Task task : created) {
                var defWal = task.getDefWalId() != null ? platformContext.getEntity(WAL.class, task.getDefWalId()) : null;
                platformContext.bind(new ShadowTask(appId, task.getId(), task.getStartAt(), defWal));
            }
            platformContext.finish();
        }
    }

}
