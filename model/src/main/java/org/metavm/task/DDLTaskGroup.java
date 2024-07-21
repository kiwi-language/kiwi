package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.ddl.Commit;
import org.metavm.entity.IEntityContext;

import java.util.List;

@EntityType
public class DDLTaskGroup extends DynamicTaskGroup {

    private final Commit commit;

    public DDLTaskGroup(Commit commit) {
        this.commit = commit;
    }

    @Override
    public List<Task> createTasks(IEntityContext context) {
        return List.of(new DDL(commit));
    }

    @Override
    protected void onTasksDone(IEntityContext context, IEntityContext taskContext) {
        commit();
        if(!commit.getValueToEntityKlassIds().isEmpty())
            taskContext.bind(new DDLCleanUpTaskGroup(commit));
    }

    public void commit() {
        commit.submit();
    }
}
