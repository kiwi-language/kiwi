package org.metavm.object.instance.core;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.TreeTags;
import org.metavm.entity.natives.NativeBase;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Type;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

@Slf4j
public abstract class MvInstance extends BaseInstance {

    private Type type;
    private transient boolean loadedFromCache;
    private transient boolean modified;


    private transient NativeBase nativeObject;

    public MvInstance(Type type, boolean isNew) {
        this(null, type, 0L, 0L, false, isNew);
    }

    public MvInstance(@Nullable Id id, Type type, long version, long syncVersion, boolean ephemeral, boolean isNew) {
        super(id, version, syncVersion, ephemeral, isNew);
        this.type = type;
    }

    public Type getInstanceType() {
        return type;
    }

    public boolean isEphemeral() {
        return state().isEphemeral() || getInstanceType().isEphemeral();
    }

    public @Nullable Id tryGetId() {
        return state.id;
    }

    public boolean isLoadedFromCache() {
        return loadedFromCache;
    }

    void setLoadedFromCache(boolean loadedFromCache) {
        this.loadedFromCache = loadedFromCache;
    }

    public boolean isRoot() {
        return !isValue() && getRoot() == this;
    }

    void ensureMutable() {
        if (isLoadedFromCache())
            throw new IllegalStateException(String.format("Instance %s is immutable", this));
    }

    @Override
    public void writeTo(MvOutput output) {
        output.write(TreeTags.DEFAULT);
        output.writeLong(getVersion());
        output.writeLong(getTreeId());
        output.writeLong(getNextNodeId());
        if (isSeparateChild()) {
            output.writeBoolean(true);
            output.writeId(requireNonNull(getParent()).getId());
        } else
            output.writeBoolean(false);
        write(output);
    }

    public void write(MvOutput output) {
        writeHead(output);
        writeBody(output);
    }

    protected void writeHead(MvOutput output) {
        if (isValue())
            output.write(WireTypes.VALUE_INSTANCE);
        else {
            output.write(WireTypes.INSTANCE);
            output.writeLong(requireNonNull(state.id).getNodeId());
        }
        getInstanceType().write(output);
    }

    protected abstract void writeBody(MvOutput output);

    public void readRecord(InstanceInput input) {
        setLoadedFromCache(input.isLoadedFromCache());
        readBody(input);
    }

    protected abstract void readBody(InstanceInput input);

    boolean isModified() {
        return modified;
    }

    void setModified() {
        ensureMutable();
        this.modified = true;
    }

    public @Nullable Instance getParent() {
        return null;
    }

    @Override
    public MvInstance getRoot() {
        return this;
    }

    public boolean isChildOf(Instance instance) {
        return !isRoot() && this.getParent() == instance;
    }

    public NativeBase getNativeObject() {
        return nativeObject;
    }

    public void setNativeObject(NativeBase nativeObject) {
        this.nativeObject = nativeObject;
    }

    @Override
    public final String toString() {
        return getInstanceType().getTypeDesc() + "-" + getTitle();
    }

    public Object toSearchConditionValue() {
        return Utils.safeCall(state.id, Id::getTreeId);
    }

    public boolean isValue() {
        return tryGetId() == null && getInstanceType().isValueType();
    }

    public boolean isSeparateChild() {
        return isRoot() && this.getParent() != null;
    }

    public abstract boolean isArray();

    public abstract String getTitle();

    public abstract void forEachReference(TriConsumer<Reference, Boolean, Type> action);

    public abstract void forEachReference(BiConsumer<Reference, Boolean> action);

    @Override
    public void transformReference(Function<Reference, Reference> function) {
        transformReference((r, isChild, type) -> function.apply(r));
    }

    public void transformReference(BiFunction<Reference, Boolean, Reference> function) {
        transformReference((r, isChild, type) -> function.apply(r, isChild));
    }

    public abstract void transformReference(TriFunction<Reference, Boolean, Type, Reference> function);

    public abstract Instance copy(Function<ClassType, Id> idSupplier);

    public String getQualifiedTitle() {
        return getInstanceType().getName() + "-" + getTitle();
    }

    @Override
    public String getText() {
        var treeWriter = new TreeWriter();
        writeTree(treeWriter);
        return treeWriter.toString();
    }


    public boolean isEnum() {
        return false;
    }

    public boolean isRemoving() {
        return state().isRemoving();
    }

    public void setType(Type type) {
        this.type = type;
    }
}
