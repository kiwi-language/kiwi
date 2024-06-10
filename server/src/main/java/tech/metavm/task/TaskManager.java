package tech.metavm.task;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.entity.EntityContextFactoryAware;
import tech.metavm.entity.IEntityContext;
import tech.metavm.util.ReflectionUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component
public class TaskManager extends EntityContextFactoryAware {

    private final TransactionOperations transactionTemplate;

    public TaskManager(EntityContextFactory entityContextFactory, TransactionOperations transactionTemplate) {
        super(entityContextFactory);
        this.transactionTemplate = transactionTemplate;
        Executor executor = Executors.newSingleThreadExecutor();
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

}
