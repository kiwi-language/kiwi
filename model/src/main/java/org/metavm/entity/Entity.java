package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.core.*;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
public abstract class Entity extends BaseInstance implements IdInitializing, RemovalAware, BindAware, NativeObject {

    private transient Entity root;

    public Entity(@NotNull Id id) {
        super(id, 0,0 , false);
        state.setNew();
    }

    @NoProxy
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
    @NoProxy
    public Id tryGetId() {
        return state.id;
    }

    public String getStringId() {
        return Utils.safeCall(getEntityId(), Id::toString);
    }

    @NoProxy
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


    @NoProxy
    public final boolean isIdNull() {
        return state.id == null;
    }

    @Override
    public final void initId(Id id) {
        throw new UnsupportedOperationException("Cannot call initId on " + getClass().getName());
//        if (tryGetPhysicalId() != null)
//            throw new IllegalStateException("objectId is already initialized");
//        this.state.id = id;
    }

    @Override
    public void clearId() {
        this.state.id = null;
    }

    @Override
    public List<Instance> beforeRemove(IInstanceContext context) {
        return List.of();
    }

    @Override
    public void onBind(IInstanceContext context) {
    }

    public boolean afterContextInitIds() {
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s, id: %s", getClass().getSimpleName(), state.id);
    }

    public Long getTmpId() {
        if (state.id instanceof TmpId tmpId)
            return tmpId.tmpId();
        else
            return null;
    }

    @Override
    public final void write(MvOutput output) {
        output.writeId(getId());
        writeBody(output);
    }

    public final void read(MvInput input, Entity parent) {
        initState(input.readId(), 0, 0, false);
        readHeadAndBody(input, parent);
    }

    public final void readHeadAndBody(MvInput input, Entity parent) {
        readBody(input, parent);
    }

    protected abstract void writeBody(MvOutput output);

    protected abstract void readBody(MvInput input, Entity parent);

    public abstract int getEntityTag();

    @Override
    public String getTitle() {
        return getClass().getSimpleName() + " " + getStringId();
    }

    @Override
    public void writeTo(MvOutput output) {
        output.write(TreeTags.ENTITY);
        output.writeLong(state.id.getTreeId());
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

    public Map<String, Object> toJson() {
        var map = new HashMap<String, Object>();
        buildJson(map);
        return map;
    }

    protected abstract void buildJson(Map<String, Object> map);

    public Map<String, Value> buildSource() {
        var source = new HashMap<String, Value>();
        buildSource(source);
        return source;
    }

    protected abstract void buildSource(Map<String, Value> source);

    @Override
    public String getText() {
        return toString();
    }

    public Id nextChildId() {
        assert isRoot();
        var treeId = tryGetTreeId();
        return treeId != null ? PhysicalId.of(treeId, nextNodeId()) : TmpId.random();
    }

}
