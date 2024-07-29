package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.ddl.Commit;
import org.metavm.ddl.CommitState;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.WAL;

import javax.annotation.Nullable;
import java.util.List;

@EntityType
public class DDLTask extends ScanTask implements WalTask {

    public static boolean DISABLE_DELAY = false;

    private final Commit commit;
    private final CommitState commitState;

    public DDLTask(Commit commit, CommitState commitState) {
        super(String.format("DDLTask-%s", commitState.name()));
        this.commit = commit;
        this.commitState = commitState;
    }

    @Override
    protected void process(List<DurableInstance> batch, IEntityContext context, IEntityContext taskContext) {
        commitState.process(batch, commit, context);
    }

    @Override
    protected void onStart(IEntityContext context, IEntityContext taskContext) {
        commitState.onStart(context, commit);
    }

    @Override
    protected void onScanOver(IEntityContext context, IEntityContext taskContext) {
        CommitState nextState;
        if(commitState.isPreparing() && commit.isCancelled())
            nextState = CommitState.ABORTING;
        else {
            commitState.onCompletion(commit);
            nextState = commitState.nextState();
            while (!nextState.isTerminal() && nextState.shouldSkip(commit))
                nextState = nextState.nextState();
        }
        commit.setState(nextState);
        if(!nextState.isTerminal()) {
            if(DISABLE_DELAY)
                taskContext.bind(new DDLTask(commit, nextState));
            else
                taskContext.bind(Tasks.delay(new DDLTask(commit, nextState)));
        }
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
    public boolean isMigrationEnabled() {
        return commitState.isMigrationEnabled();
    }

    @Override
    public long getTimeout() {
        return commitState.getSessionTimeout();
    }
}
