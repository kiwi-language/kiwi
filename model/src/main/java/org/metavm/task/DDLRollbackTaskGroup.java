package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.entity.IEntityContext;

import java.util.List;

@EntityType
public class DDLRollbackTaskGroup extends TaskGroup {
    @Override
    public List<Task> createTasks(IEntityContext context) {
        return List.of(new DDLRollbackTask());
    }

    @Override
    protected void onCompletion(IEntityContext context, IEntityContext taskContext) {

    }
}