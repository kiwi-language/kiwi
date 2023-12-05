package tech.metavm.task;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.util.Constants;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component
public class TaskManager {

    private final InstanceContextFactory instanceContextFactory;

    private final TransactionOperations transactionTemplate;

    public TaskManager(InstanceContextFactory instanceContextFactory, TransactionOperations transactionTemplate) {
        this.instanceContextFactory = instanceContextFactory;
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

    private void executeTask() {
        transactionTemplate.executeWithoutResult(status -> {
            IEntityContext context = newContext();
            List<FieldRemovalTask> tasks = context.getByType(FieldRemovalTask.class, null, 1);
            if (NncUtils.isEmpty(tasks)) {
                return;
            }
            FieldRemovalTask task = tasks.get(0);
            if (task.executeBatch(context.getInstanceContext())) {
                context.remove(task);
            }
            context.finish();
        });
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
            Class<? extends Task> taskClass = ReflectUtils.classForName(taskClassName).asSubclass(Task.class);
        }
    }

    private IEntityContext newContext() {
        return instanceContextFactory.newEntityContext();
    }

    private IEntityContext newPlatformContext() {
        return instanceContextFactory.newEntityContext(Constants.PLATFORM_APP_ID);
    }

}
