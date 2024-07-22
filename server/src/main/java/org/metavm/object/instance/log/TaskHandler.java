package org.metavm.object.instance.log;

import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.entity.IEntityContext;
import org.metavm.task.ShadowTask;
import org.metavm.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionOperations;

import javax.annotation.Nullable;
import java.util.List;

@Component
public class TaskHandler extends EntityContextFactoryAware implements LogHandler<Task>  {

    public static final Logger logger = LoggerFactory.getLogger(TaskHandler.class);

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
    public void process(List<Task> created, @Nullable String clientId, IEntityContext context, EntityContextFactory entityContextFactory) {
        transactionOperations.executeWithoutResult(s -> {
            try (var platformContext = newPlatformContext()) {
                for (Task task : created) {
                    platformContext.bind(new ShadowTask(context.getAppId(), task.getStringId(), task.getStartAt()));
                }
                platformContext.finish();
            }
        });
    }


}
