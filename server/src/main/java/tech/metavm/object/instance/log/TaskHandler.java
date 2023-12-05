package tech.metavm.object.instance.log;

import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.IInstanceContextFactory;
import tech.metavm.task.Task;
import tech.metavm.task.TaskSignal;
import tech.metavm.util.Constants;
import tech.metavm.util.NncUtils;

import java.util.List;

@Component
public class TaskHandler implements LogHandler<Task>  {

    private final IInstanceContextFactory instanceContextFactory;

    private final TransactionOperations transactionOperations;

    public TaskHandler(IInstanceContextFactory instanceContextFactory, TransactionOperations transactionOperations) {
        this.instanceContextFactory = instanceContextFactory;
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
                TaskSignal signal = NncUtils.requireNonNull(platformContext.selectByUniqueKey(TaskSignal.IDX_APP_ID, context.getAppId()));
                signal.setUnfinishedCount(signal.getUnfinishedCount() + created.size());
                signal.setLastTaskCreatedAt(System.currentTimeMillis());
                platformContext.finish();
            }
        });
    }

    private IEntityContext newPlatformContext() {
        return instanceContextFactory.newEntityContext(Constants.PLATFORM_APP_ID);
    }

}
