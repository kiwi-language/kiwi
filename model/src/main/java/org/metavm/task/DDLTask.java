package org.metavm.task;

import lombok.extern.slf4j.Slf4j;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.ddl.Commit;
import org.metavm.ddl.CommitState;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(47)
@Slf4j
@Entity
public class DDLTask extends ScanTask implements IDDLTask {

    public static boolean DISABLE_DELAY = true;
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private Reference commitReference;
    private CommitState commitState;

    public DDLTask(Commit commit, CommitState commitState) {
        super(String.format("DDLTask-%s", commitState.name()));
        this.commitReference = commit.getReference();
        this.commitState = commitState;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        ScanTask.visitBody(visitor);
        visitor.visitValue();
        visitor.visitByte();
    }

    @Override
    protected void process(List<Instance> batch, IEntityContext context, IEntityContext taskContext) {
        commitState.process(batch, getCommit(), context);
    }

    @Override
    protected void onStart(IEntityContext context, IEntityContext taskContext) {
        commitState.onStart(context, getCommit());
    }

    @Override
    protected void onScanOver(IEntityContext context, IEntityContext taskContext) {
        commitState.transition(getCommit(), taskContext);
    }

    @Override
    protected void onFailure(IEntityContext context, IEntityContext taskContext) {
        var commit = getCommit();
        if (commit.getState() != CommitState.ABORTING) {
            commit.setState(CommitState.ABORTING);
            taskContext.bind(new DDLTask(getCommit(), CommitState.ABORTING));
        }
        else
            log.error("Failed to rollback DDL {}", commit.getId());
    }

    public CommitState getCommitState() {
        return commitState;
    }

    public Commit getCommit() {
        return (Commit) commitReference.get();
    }

    @Nullable
    @Override
    public WAL getWAL() {
        return commitState.isPreparing() ? getCommit().getWal() : null;
    }

    @Nullable
    @Override
    public WAL getMetaWAL() {
        return getWAL();
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
    public void buildJson(Map<String, Object> map) {
        map.put("commitState", this.getCommitState().name());
        map.put("commit", this.getCommit().getStringId());
        var wAL = this.getWAL();
        if (wAL != null) map.put("wAL", wAL.getStringId());
        var metaWAL = this.getMetaWAL();
        if (metaWAL != null) map.put("metaWAL", metaWAL.getStringId());
        map.put("relocationEnabled", this.isRelocationEnabled());
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
        var defWalId = this.getDefWalId();
        if (defWalId != null) map.put("defWalId", defWalId);
        map.put("extraStdKlassIds", this.getExtraStdKlassIds());
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
        return EntityRegistry.TAG_DDLTask;
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