package org.metavm.task;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.ddl.Commit;
import org.metavm.ddl.CommitState;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(38)
@Entity
public class SimpleDDLTask extends Task implements IDDLTask {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private Reference commitReference;
    private CommitState commitState;

    public SimpleDDLTask(Id id, Commit commit, CommitState commitState) {
        super(id, "SimpleDDLTask-" + commitState.name());
        this.commitReference = commit.getReference();
        this.commitState = commitState;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        Task.visitBody(visitor);
        visitor.visitValue();
        visitor.visitByte();
    }

    @Override
    protected boolean run0(IInstanceContext context, IInstanceContext taskContext) {
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
    public void buildJson(Map<String, Object> map) {
        map.put("commit", this.getCommit().getStringId());
        map.put("commitState", this.getCommitState().name());
        map.put("timeout", this.getTimeout());
        var group = this.getGroup();
        if (group != null) map.put("group", group.getStringId());
        map.put("runCount", this.getRunCount());
        map.put("state", this.getState().name());
        map.put("runnable", this.isRunnable());
        map.put("running", this.isRunning());
        map.put("completed", this.isCompleted());
        map.put("failed", this.isFailed());
        map.put("terminated", this.isTerminated());
        map.put("lastRunTimestamp", this.getLastRunTimestamp());
        map.put("startAt", this.getStartAt());
        map.put("extraStdKlassIds", this.getExtraStdKlassIds());
        map.put("relocationEnabled", this.isRelocationEnabled());
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_SimpleDDLTask;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        super.readBody(input, parent);
        this.commitReference = (Reference) input.readValue();
        this.commitState = CommitState.fromCode(input.read());
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeValue(commitReference);
        output.write(commitState.code());
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        super.buildSource(source);
    }
}
