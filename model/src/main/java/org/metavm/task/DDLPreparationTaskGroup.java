package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.ddl.Commit;
import org.metavm.entity.IEntityContext;

import java.util.List;

@EntityType
public class DDLPreparationTaskGroup extends DynamicTaskGroup {

    private final Commit commit;

    public DDLPreparationTaskGroup(Commit commit) {
        this.commit = commit;
    }

    @Override
    public List<Task> createTasks(IEntityContext context) {
        return List.of(new DDLPreparationTask(commit));
    }

    @Override
    protected void onCompletion(IEntityContext context, IEntityContext taskContext) {
        commit.submit();
        if(!commit.getValueToEntityKlassIds().isEmpty())
            taskContext.bind(new DDLFinalizationTaskGroup(commit));
    }

    @Override
    public long getSessionTimeout() {
        return 5000L;
    }
}
