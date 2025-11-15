package org.metavm.task;

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

@Wire(38)
@Slf4j
@Entity
public class SimpleDDLTask extends Task implements IDDLTask {

    private final Reference commitReference;
    private final CommitState commitState;

    public SimpleDDLTask(Id id, Commit commit, CommitState commitState) {
        super(id, "SimpleDDLTask-" + commitState.name());
        this.commitReference = commit.getReference();
        this.commitState = commitState;
    }

    @Override
    protected boolean run1(IInstanceContext context, IInstanceContext taskContext) {
        commitState.process(List.of(), getCommit(), context);
        commitState.transition(getCommit(), taskContext);
        return true;
    }

    @Override
    public Commit getCommit() {
        return (Commit) commitReference.get();
    }

    @Override
    public CommitState getCommitState() {
        return commitState;
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
