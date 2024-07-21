package org.metavm.task;

import org.jetbrains.annotations.NotNull;
import org.metavm.ddl.Commit;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.InstanceReference;
import org.metavm.object.instance.core.WAL;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class DDL extends ScanTask implements WalTask {

    private static final Logger logger = LoggerFactory.getLogger(DDL.class);

    private final Commit commit;

    DDL(Commit commit) {
        super("DDL " + NncUtils.formatDate(commit.getTime()));
        this.commit = commit;
    }

    @Override
    protected List<InstanceReference> scan(IInstanceContext context, long cursor, long limit) {
        return context.scan(cursor, limit);
    }

    @Override
    protected void process(List<InstanceReference> batch, IEntityContext context, IEntityContext taskContext) {
        var tasks = Instances.applyDDL(
                () -> batch.stream()
                        .map(InstanceReference::resolve)
                        .iterator(),
                commit, context);
        for (Task task : tasks) {
            taskContext.bind(task);
            getGroup().addTask(task);
        }
    }

    @Override
    protected void onFailure(IEntityContext context, IEntityContext taskContext) {
        taskContext.bind(new DDLRollbackTaskGroup());
    }

    @Override
    public @NotNull DDLTaskGroup getGroup() {
        return (DDLTaskGroup) Objects.requireNonNull(super.getGroup());
    }

    public void commit() {
        commit.submit();
    }

    @Override
    public WAL getWAL() {
        return commit.getWal();
    }

    @Override
    public boolean isMigrationDisabled() {
        return true;
    }

}
