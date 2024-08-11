package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.ddl.Commit;
import org.metavm.ddl.CommitState;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.WAL;

import javax.annotation.Nullable;
import java.util.List;

@EntityType
public class DDLTask extends ScanTask implements IDDLTask {

    public static boolean DISABLE_DELAY = true;

    private final Commit commit;
    private final CommitState commitState;

    public DDLTask(Commit commit, CommitState commitState) {
        super(String.format("DDLTask-%s", commitState.name()));
        this.commit = commit;
        this.commitState = commitState;
    }

    @Override
    protected void process(List<Instance> batch, IEntityContext context, IEntityContext taskContext) {
        commitState.process(batch, commit, context);
    }

    @Override
    protected void onStart(IEntityContext context, IEntityContext taskContext) {
        commitState.onStart(context, commit);
    }

    @Override
    protected void onScanOver(IEntityContext context, IEntityContext taskContext) {
        commitState.transition(commit, taskContext);
    }

    @Override
    protected void onFailure(IEntityContext context, IEntityContext taskContext) {
        if (commit.getState() != CommitState.ABORTING) {
            commit.setState(CommitState.ABORTING);
            taskContext.bind(new DDLTask(commit, CommitState.ABORTING));
        }
        else
            logger.error("Failed to rollback DDL {}", commit.getId());
    }

    public CommitState getCommitState() {
        return commitState;
    }

    public Commit getCommit() {
        return commit;
    }

    @Nullable
    @Override
    public WAL getWAL() {
        return commitState.isPreparing() ? commit.getWal() : null;
    }

    @Override
    public boolean isRelocationEnabled() {
        return commitState.isRelocationEnabled();
    }

    @Override
    public long getTimeout() {
        return commitState.getSessionTimeout();
    }
}
