package org.metavm.object.instance.core;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.TreeTags;
import org.metavm.entity.natives.NativeBase;
import org.metavm.object.instance.rest.InstanceDTO;
import org.metavm.object.instance.rest.InstanceParam;
import org.metavm.object.type.Field;
import org.metavm.object.type.Type;
import org.metavm.object.type.rest.dto.InstanceParentRef;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

@Slf4j
public abstract class MvInstance extends BaseInstance {

    private Type type;
    private transient boolean loadedFromCache;
    private transient boolean modified;
    private transient Object mappedEntity;

    private @Nullable Field parentField;
    private @NotNull MvInstance root;
    
    private MvInstance parent;
    private @Nullable MvInstance oldRoot;
    private @Nullable Field oldParentField;
    private @Nullable Id oldId;
    private @Nullable Id relocatedId;
    private @NotNull MvInstance aggregateRoot;
    private boolean pendingChild;
    private boolean useOldId;

    private transient NativeBase nativeObject;

    public MvInstance(Type type) {
        this(null, type, 0L, 0L, false, null);
    }

    public MvInstance(@Nullable Id id, Type type, long version, long syncVersion, boolean ephemeral, @Nullable Consumer<Instance> load) {
        super(id, version, syncVersion, ephemeral);
        this.type = type;
        root = aggregateRoot = this;
    }

    public Type getInstanceType() {
        return type;
    }

    public boolean isEphemeral() {
        return state().isEphemeral() || getInstanceType().isEphemeral();
    }

    public @Nullable Id tryGetId() {
        return useOldId ? oldId : state.id;
    }

    public Id getCurrentId() {
        return requireNonNull(state.id);
    }

    public @Nullable Id tryGetCurrentId() {
        return state.id;
    }

    public Object getMappedEntity() {
        return mappedEntity;
    }

    public void setMappedEntity(Object mappedEntity) {
        this.mappedEntity = mappedEntity;
    }

    public boolean isPersisted() {
        return !isNew();
    }

    public boolean isLoadedFromCache() {
        return loadedFromCache;
    }

    void setLoadedFromCache(boolean loadedFromCache) {
        this.loadedFromCache = loadedFromCache;
    }

    public void setParentRef(@Nullable InstanceParentRef parentRef) {
        if (parentRef != null)
            setParent(parentRef.parent().get(), parentRef.field());
    }

    @Override
    public void setParent(Instance parent, @Nullable Field parentField) {
        if (this.parent != null) {
            if (this.parent.equals(parent) && Objects.equals(this.parentField, parentField))
                return;
            throw new InternalException("Can not change parent of " + Instances.getInstanceDesc(getReference())
                    + ", current parent: " + this.parent
                    + ", current parentField: " + this.parentField
                    + ", new parent: " + parent
                    + ", new parentField: " + parentField
            );
        }
        setParentInternal((MvInstance) parent, parentField, state.id == null || !state.id.isRoot());
    }

    public void setParentInternal(@Nullable InstanceParentRef parentRef) {
        if (parentRef != null)
            setParentInternal((MvInstance) parentRef.parent().get(), parentRef.field(), true);
        else
            setParentInternal(null, null, true);
    }

    public void setParentInternal(@Nullable MvInstance parent, @Nullable Field parentField, boolean setRoot) {
        if (parent == this.parent && parentField == this.parentField)
            return;
        if (parent != null) {
            this.parent = parent;
            if (parent instanceof ClassInstance) {
                this.parentField = parentField;
//                assert parentField.isChild() : "Invalid parent field: " + parentField;
            } else if (parent instanceof ArrayInstance parentArray) {
                Utils.require(parentField == null);
                assert parentArray.isChildArray();
                this.parentField = null;
            } else
                throw new IllegalArgumentException("Invalid parent: " + parent);
            if (setRoot)
                root = (MvInstance) parent.getRoot();
            if (!pendingChild)
                aggregateRoot = parent;
            if (parent.isEphemeral() && !state.isEphemeral())
                forEachDescendant(instance -> instance.state().setEphemeral());
        } else {
            this.parent = null;
            this.parentField = null;
            if (setRoot)
                aggregateRoot = root = this;
        }
    }

    public void clearParent() {
        this.parent = null;
        this.parentField = null;
    }

    public boolean isRoot() {
        return !isInlineValue() && getRoot() == this;
    }

    public boolean isReferencedByParent() {
        if(this.parent != null) {
            var ref = new Object() {
                boolean referenced;
            };
            this.parent.forEachChild(c -> {
                if(c == this)
                    ref.referenced = true;
            });
            return ref.referenced;
        }
        else
            return false;
    }

    public boolean canMerge() {
        return !pendingChild && this.parent != null && isRoot();
    }

    public boolean canExtract() {
        return !isValue() && !isRoot() && this.parent != null && parentField != null && !parentField.isChild();
    }

    public void initId(Id id) {
        if (isIdInitialized())
            throw new InternalException("id already initialized");
        super.initId(id);
    }

    void ensureMutable() {
        if (isLoadedFromCache())
            throw new IllegalStateException(String.format("Instance %s is immutable", this));
    }

    public Set<Instance> getChildren() {
        return Set.of();
    }

    @Override
    public void writeTo(MvOutput output) {
        output.write(TreeTags.DEFAULT);
        // !!! IMPORTANT: Version must starts at the second byte. @see org.metavm.entity.ContextDifference.incVersion !!!
        output.writeLong(getVersion());
        output.writeLong(getTreeId());
        output.writeLong(getNextNodeId());
        if (isSeparateChild()) {
            output.writeBoolean(true);
            output.writeId(requireNonNull(getParent()).getId());
            output.writeId(requireNonNull(getParentField()).getId());
        } else
            output.writeBoolean(false);
        write(output);
    }

    public void write(MvOutput output) {
        writeHead(output);
        writeBody(output);
    }

    protected void writeHead(MvOutput output) {
        if (isInlineValue())
            output.write(WireTypes.VALUE_INSTANCE);
        else {
            if (oldId != null) {
                output.write(WireTypes.RELOCATING_INSTANCE);
                output.writeLong(oldId.getTreeId());
                output.writeLong(oldId.getNodeId());
                output.writeBoolean(useOldId);
            }
            else if(isRemoving())
                output.write(WireTypes.REMOVING_INSTANCE);
            else
                output.write(WireTypes.INSTANCE);
            output.writeLong(state.id.getNodeId());
        }
        getInstanceType().write(output);
    }

    protected abstract void writeBody(MvOutput output);

    public void readRecord(InstanceInput input) {
        setLoadedFromCache(input.isLoadedFromCache());
        readFrom(input);
    }

    protected abstract void readFrom(InstanceInput input);

    boolean isModified() {
        return modified;
    }

    void setModified() {
        ensureMutable();
        this.modified = true;
    }

    public @Nullable Instance getParent() {
        return this.parent;
    }

    public @Nullable Instance getParent(int index) {
        var v = this.parent;
        for (int i = 0; i < index; i++) {
            v = requireNonNull(v).parent;
        }
        return v;
    }

    @Override
    public MvInstance getRoot() {
        if (root == this)
            return this;
        else
            return root = root.getRoot();
    }

    public boolean isChildOf(Instance instance, @Nullable Field parentField) {
        return !isRoot() && this.parent == instance && this.parentField == parentField;
    }

    @Nullable
    public Field getParentField() {
        return parentField;
    }

    public @Nullable InstanceParentRef getParentRef() {
        return this.parent == null ? null : new InstanceParentRef(this.parent.getReference(), parentField);
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
        return state.id.getTreeId();
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isValue() {
        return getInstanceType().isValueType();
    }

    public boolean isInlineValue() {
        return state.id == null && isValue();
    }

    public boolean isDetachedValue() {
        return state.id != null && isValue();
    }

    public Id getOldId() {
        return requireNonNull(oldId);
    }

    public @Nullable Id tryGetOldId() {
        return oldId;
    }

    public void setOldId(@Nullable Id oldId) {
        this.oldId = oldId;
    }

    public void setUseOldId(boolean useOldId) {
        this.useOldId = useOldId;
    }

    public boolean isUseOldId() {
        return useOldId;
    }

    void clearOldId() {
        assert oldId != null;
        this.oldId = null;
    }

    public boolean isSeparateChild() {
        return isRoot() && this.parent != null;
    }

    public boolean isPendingChild() {
        return pendingChild;
    }

    public Instance getAggregateRoot() {
        if (aggregateRoot == this)
            return this;
        else
            return aggregateRoot = (MvInstance) aggregateRoot.getAggregateRoot();
    }

    public void merge() {
        this.oldId = state.id;
        this.oldRoot = root;
        this.root = (MvInstance) requireNonNull(this.parent).getRoot();
        state.id = relocatedId != null ? relocatedId : PhysicalId.of(root.getTreeId(), root.nextNodeId());
        useOldId = true;
    }

    public void rollbackMerge() {
        relocatedId = state.id;
        state.id = oldId;
        oldId = null;
        useOldId = false;
        root = requireNonNull(oldRoot);
        oldRoot = null;
    }

    public void extract(boolean isRoot) {
        this.oldId = state.id;
        state.id = relocatedId;
        useOldId = true;
        if (isRoot) {
            this.root = aggregateRoot = this;
            oldRoot = this.parent;
            oldParentField = parentField;
            this.parent = null;
            parentField = null;
        } else {
            this.oldRoot = this.root;
            this.root = aggregateRoot = requireNonNull(this.parent);
        }
    }

    public void rollbackExtraction() {
        var isRoot = isRoot();
        relocatedId = state.id;
        state.id = oldId;
        oldId = null;
        useOldId = false;
        if (isRoot) {
            root = aggregateRoot = this.parent = requireNonNull(oldRoot);
            parentField = oldParentField;
            oldParentField = null;
        } else
            root = aggregateRoot = requireNonNull(oldRoot);
        oldRoot = null;
    }

    public void switchId() {
        useOldId = false;
    }

    public void writeForwardingPointers(InstanceOutput output) {
        output.write(TreeTags.RELOCATED);
        output.writeId(requireNonNull(oldId));
        output.writeId(state.id);
    }

    public void setPendingChild(boolean pendingChild) {
        this.pendingChild = pendingChild;
    }

    public Reference getReference() {
        var ref = new Reference(this);
        if(oldId != null && useOldId)
            ref.setForwarded();
        return ref;
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

    public abstract InstanceParam getParam();

    public String getStringIdForDTO() {
        return getStringId();
    }

    public InstanceDTO toDTO() {
        return toDTO(getParam());
    }

    protected InstanceDTO toDTO(InstanceParam param) {
        try (var serContext = SerializeContext.enter()) {
            return new InstanceDTO(
                    getStringIdForDTO(),
                    getInstanceType().toExpression(serContext),
                    getInstanceType().getName(),
                    getTitle(),
                    param
            );
        }
    }

    public abstract Instance copy();

    public String getQualifiedTitle() {
        return getInstanceType().getName() + "-" + getTitle();
    }

    public String getText() {
        var treeWriter = new TreeWriter();
        writeTree(treeWriter);
        return treeWriter.toString();
    }

    public Id getRelocatedId() {
        return requireNonNull(relocatedId);
    }

    public boolean isEnum() {
        return false;
    }

    public boolean isRemoving() {
        return state().isRemoving();
    }
}
