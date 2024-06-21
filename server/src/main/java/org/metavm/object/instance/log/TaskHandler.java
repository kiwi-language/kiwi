package org.metavm.object.instance.log;

import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.entity.IEntityContext;
import org.metavm.task.ShadowTask;
import org.metavm.task.Task;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionOperations;

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
                for (Task task : created)
                    platformContext.bind(new ShadowTask(context.getAppId(), task.getStringId()));
                platformContext.finish();
            }
        });
    }


}
