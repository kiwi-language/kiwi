package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;
import org.metavm.util.Utils;
import org.metavm.wire.*;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
@Wire
public abstract class Entity implements Instance, IdInitializing, BindAware, NativeObject {

    private transient Entity root;
    private final Id id;
    private int refcount;
    private transient InstanceState state;

    public Entity(@NotNull Id id) {
        this.id = id;
        state = new InstanceState(id, 0, 0, false, true, this);
    }

    /** @noinspection unused*/
    private void onRead() {
        state = new InstanceState(id, 0, 0, false, false, this);
    }

    public void initState(Id id, long version ,long syncVersion, boolean ephemeral, boolean isNew) {
        state = new InstanceState(id, version, syncVersion, ephemeral, isNew, this);
    }

    /** @noinspection unused*/
    protected static void __visit__(WireVisitor visitor, WireAdapter<Id> idAdapter) {
        var streamVisitor = (StreamVisitor) visitor;
        streamVisitor.visitEntityHead();
    }

    @Override
    public final InstanceState state() {
        return state;
    }

    public boolean idEquals(Id id) {
        return Objects.equals(state.id, id);
    }

    public boolean isIdNotNull() {
        return state.id != null;
    }

    public Id getEntityId() {
        return state.id;
    }

    @Nullable
    @Override
    public Id tryGetId() {
        return state.id;
    }

    public String getStringId() {
        return Utils.safeCall(getEntityId(), Id::toString);
    }

    public final EntityKey key() {
        if (state.id == null) {
            return null;
        }
        return EntityKey.create(this.getClass(), state.id);
    }

    public abstract @Nullable Entity getParentEntity();

    public Entity getRootEntity() {
        Entity root = this;
        while (root.getParentEntity() != null)
            root = root.getParentEntity();
        return root;
    }

    @Override
    public boolean isEphemeral() {
        return state().isEphemeral();
    }


    public final boolean isIdNull() {
        return state.id == null;
    }

    @Override
    public void onBind(IInstanceContext context) {
    }

    @Override
    public String toString() {
        return String.format("%s, id: %s, version: %s", getClass().getSimpleName(), state.id, getVersion());
    }

    public Long getTmpId() {
        if (state.id instanceof TmpId tmpId)
            return tmpId.tmpId();
        else
            return null;
    }

    @Override
    public final void write(MvOutput output) {
        output.writeEntity(this);
    }

    @Override
    public String getTitle() {
        return getClass().getSimpleName() + " " + getStringId();
    }

    @Override
    public void writeTo(MvOutput output) {
        output.write(TreeTags.ENTITY);
        output.writeLong(getVersion());
        output.writeLong(Objects.requireNonNull(state.id).getTreeId());
        output.writeLong(getNextNodeId());
        output.writeEntity(this);
    }

    @Override
    public void forEachValue(Consumer<? super Instance> action) {
    }

    @Override
    public <R> R accept(InstanceVisitor<R> visitor) {
        return visitor.visitEntity(this);
    }

    @Nullable
    @Override
    public Instance getParent() {
        return getParentEntity();
    }

    @Override
    public boolean isRoot() {
        return getParentEntity() == null;
    }

    @Override
    public Entity getRoot() {
        var parent = getParentEntity();
        if (parent == null)
            return this;
        if (root == null)
            root = parent;
        return root = root.getRoot();
    }

    public Map<String, Value> buildSource() {
        var source = new HashMap<String, Value>();
        buildSource(source);
        return source;
    }

    protected void buildSource(Map<String, Value> source) {
    }

    @Override
    public String getText() {
        return toString();
    }

    public Id nextChildId() {
        assert isRoot();
        var treeId = tryGetTreeId();
        return treeId != null ? PhysicalId.of(treeId, nextNodeId()) : TmpId.random();
    }

    @Override
    public EntityReference getReference() {
        return new EntityReference(this);
    }

    @Override
    public void incRefcount(int amount) {
        refcount += amount;
    }

    @Override
    public int getRefcount() {
        return refcount;
    }

    @Override
    public Klass getInstanceKlass() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ClassType getInstanceType() {
        throw new UnsupportedOperationException("Not implemented");
    }

    // Forbid overriding by mistake
    @Override
    public final void setVersion(long version) {
        state().setVersion(version);

    }
}
