package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.ddl.Commit;
import org.metavm.ddl.CommitState;
import org.metavm.entity.IEntityContext;

import java.util.List;

@EntityType
public class SimpleDDLTask extends Task implements IDDLTask {

    private final Commit commit;
    private final CommitState commitState;

    public SimpleDDLTask(Commit commit, CommitState commitState) {
        super("SimpleDDLTask-" + commitState.name());
        this.commit = commit;
        this.commitState = commitState;
    }

    @Override
    protected boolean run0(IEntityContext context, IEntityContext taskContext) {
        commitState.process(List.of(), commit, context);
        commitState.transition(commit, taskContext);
        return true;
    }

    @Override
    public Commit getCommit() {
        return commit;
    }

    @Override
    public CommitState getCommitState() {
        return commitState;
    }

    @Override
    public long getTimeout() {
        return commitState.getSessionTimeout();
    }

}
