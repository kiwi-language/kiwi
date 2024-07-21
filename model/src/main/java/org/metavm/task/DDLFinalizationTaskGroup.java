package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.ddl.Commit;
import org.metavm.entity.IEntityContext;

import java.util.List;

@EntityType
public class DDLFinalizationTaskGroup extends TaskGroup {

    private final Commit commit;

    public DDLFinalizationTaskGroup(Commit commit) {
        this.commit = commit;
    }

    @Override
    public List<Task> createTasks(IEntityContext context) {
        return List.of(new DDLFinalizationTask(commit));
    }

    @Override
    protected void onCompletion(IEntityContext context, IEntityContext taskContext) {
        commit.finish();
    }
}
