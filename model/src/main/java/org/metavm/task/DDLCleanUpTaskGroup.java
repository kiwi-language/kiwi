package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.ddl.Commit;
import org.metavm.entity.IEntityContext;

import java.util.ArrayList;
import java.util.List;

@EntityType
public class DDLCleanUpTaskGroup extends TaskGroup {

    private final Commit commit;

    public DDLCleanUpTaskGroup(Commit commit) {
        this.commit = commit;
    }

    @Override
    public List<Task> createTasks(IEntityContext context) {
        var tasks = new ArrayList<Task>();
        if(!commit.getValueToEntityKlassIds().isEmpty())
            tasks.add(new EagerFlagClearer(commit.getValueToEntityKlassIds()));
        return tasks;
    }

    @Override
    protected void onCompletion(IEntityContext context, IEntityContext taskContext) {
        commit.finish();
    }
}
