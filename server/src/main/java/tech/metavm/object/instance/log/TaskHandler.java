package tech.metavm.object.instance.log;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.entity.EntityContextFactoryAware;
import tech.metavm.entity.IEntityContext;
import tech.metavm.task.Task;
import tech.metavm.task.TaskSignal;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

@Component
public class TaskHandler extends EntityContextFactoryAware implements LogHandler<Task>  {

    private final TransactionOperations transactionOperations;

    public TaskHandler(EntityContextFactory entityContextFactory, TransactionOperations transactionOperations) {
        super(entityContextFactory);
        this.transactionOperations = transactionOperations;
    }

    @Override
    public Class<Task> getEntityClass() {
        return Task.class;
    }

    @Override
    public void process(List<Task> created, @Nullable String clientId, IEntityContext context) {
        transactionOperations.executeWithoutResult(s -> {
            try (var platformContext = newPlatformContext()) {
                TaskSignal signal = NncUtils.requireNonNull(platformContext.selectFirstByKey(TaskSignal.IDX_APP_ID, context.getAppId()));
                signal.setUnfinishedCount(signal.getUnfinishedCount() + created.size());
                signal.setLastTaskCreatedAt(System.currentTimeMillis());
                platformContext.finish();
            }
        });
    }


}
