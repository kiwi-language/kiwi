package tech.metavm.job;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component
public class JobManager {

    private final InstanceContextFactory instanceContextFactory;

    private final TransactionOperations transactionTemplate;

    public JobManager(InstanceContextFactory instanceContextFactory, TransactionOperations transactionTemplate) {
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

    private void executeJob() {
        transactionTemplate.executeWithoutResult(status -> {
            IEntityContext context = newContext();
            List<FieldRemovalJob> jobs = context.getByType(FieldRemovalJob.class, null, 1);
            if(NncUtils.isEmpty(jobs)) {
                return;
            }
            FieldRemovalJob job = jobs.get(0);
            if(job.executeBatch(context.getInstanceContext())) {
                context.remove(job);
            }
            context.finish();
        });
    }

    @Transactional
    public void addGlobalJob(Job job) {
        IEntityContext context = newRootContext();
        context.bind(job);
        context.finish();
    }

    @Transactional
    public void runJob(String jobClassName) {
        IEntityContext context = newContext();
        Class<? extends Job> jobClass = ReflectUtils.classForName(jobClassName).asSubclass(Job.class);

    }

    private IEntityContext newContext() {
        return instanceContextFactory.newContext().getEntityContext();
    }

    private IEntityContext newRootContext() {
        return instanceContextFactory.newRootContext().getEntityContext();
    }

}
