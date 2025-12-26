package org.metavm.task;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.metavm.api.Entity;
import org.metavm.ddl.Commit;
import org.metavm.ddl.CommitState;
import org.metavm.wire.Wire;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;

import java.util.List;
import java.util.function.Consumer;

@Wire(47)
@Slf4j
@Entity
public class DDLTask extends ScanTask implements IDDLTask {

    public static boolean DISABLE_DELAY = true;

    private final Reference commitReference;
    @Getter
    private final CommitState commitState;

    public DDLTask(Id id, Commit commit, CommitState commitState) {
        super(id, String.format("DDLTask-%s", commitState.name()));
        this.commitReference = commit.getReference();
        this.commitState = commitState;
    }

    @Override
    protected void process(List<Instance> batch, IInstanceContext context, IInstanceContext taskContext) {
        commitState.process(batch, getCommit(), context);
    }

    @Override
    protected void onStart(IInstanceContext context, IInstanceContext taskContext) {
        commitState.onStart(context, getCommit());
    }

    @Override
    protected void onScanOver(IInstanceContext context, IInstanceContext taskContext) {
        commitState.transition(getCommit(), taskContext);
    }

    @Override
    protected void onFailure(IInstanceContext taskContext) {
        var commit = getCommit();
        if (commit.getState() != CommitState.ABORTING) {
            commit.setState(CommitState.ABORTING);
            taskContext.bind(CommitState.ABORTING.createTask(getCommit(), taskContext));
        }
        else
            log.error("Failed to rollback DDL {}", commit.getId());
    }

    public Commit getCommit() {
        return (Commit) commitReference.get();
    }

    @Override
    public boolean isMigrating() {
        return commitState == CommitState.MIGRATING || commitState == CommitState.REMOVING;
    }

    @Override
    public boolean isRelocationEnabled() {
        return commitState.isRelocationEnabled();
    }

    @Override
    public long getTimeout() {
        return commitState.getSessionTimeout();
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        action.accept(commitReference);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

}