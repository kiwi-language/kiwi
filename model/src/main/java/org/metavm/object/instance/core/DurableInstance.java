package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.Tree;
import org.metavm.entity.TreeTags;
import org.metavm.object.instance.rest.InstanceDTO;
import org.metavm.object.instance.rest.InstanceParam;
import org.metavm.object.type.Field;
import org.metavm.object.type.Type;
import org.metavm.object.type.rest.dto.InstanceParentRef;
import org.metavm.object.view.rest.dto.MappingKey;
import org.metavm.system.RegionConstants;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public abstract class DurableInstance implements Message {

    public static final Logger logger = LoggerFactory.getLogger(DurableInstance.class);

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    private Type type;
    private transient boolean marked;
    private transient boolean viewSaved;
    private transient boolean _new;
    private transient boolean loaded;
    private transient boolean loadedFromCache;
    private transient boolean removed;
    private transient boolean modified;
    private transient boolean ephemeral;
    private transient boolean changeNotified;
    private transient boolean removalNotified;

    transient IInstanceContext context;
    private transient boolean afterContextInitIdsNotified;
    private @Nullable
    transient DurableInstance prev;
    private @Nullable
    transient DurableInstance next;
    private transient Object mappedEntity;

    private Id id;

    private long version;
    private long syncVersion;

    private @Nullable DurableInstance parent;
    private @Nullable Field parentField;
    private @NotNull DurableInstance root;
    private @Nullable DurableInstance oldRoot;
    private @Nullable Field oldParentField;
    private @Nullable Id oldId;
    private @Nullable Id migratedId;
    private @NotNull DurableInstance aggregateRoot;
    private boolean pendingChild;
    private boolean useOldId;

    private transient Long tmpId;

    private transient Object nativeObject;

    private final @Nullable Consumer<DurableInstance> load;

    private transient @Nullable SourceRef sourceRef;

    private int seq;

    private long nextNodeId = 1;

    public DurableInstance(Type type) {
        this(null, type, 0L, 0L, false, null);
    }

    public DurableInstance(@Nullable Id id, Type type, long version, long syncVersion, boolean ephemeral, @Nullable Consumer<DurableInstance> load) {
        this.type = type;
        this.version = version;
        this.syncVersion = syncVersion;
        this.load = load;
        this.ephemeral = ephemeral;
        if (id != null) {
            initId(id);
            _new = id.tryGetTreeId() == null;
        } else
            _new = true;
        this.root = aggregateRoot = this;
    }

    public boolean isDurable() {
        return !isEphemeral();
    }

    public Type getType() {
        return type;
    }

    public boolean isEphemeral() {
        return ephemeral || getType().isEphemeral();
    }

    public boolean shouldSkipWrite() {
        return isInitialized() && isEphemeral();
    }

    public @Nullable InstanceReference tryGetSource() {
        return NncUtils.get(sourceRef, SourceRef::source);
    }

    public @NotNull InstanceReference getSource() {
        return Objects.requireNonNull(tryGetSource());
    }

    public void setSourceRef(@Nullable SourceRef sourceRef) {
        this.sourceRef = sourceRef;
    }

    public SourceRef getSourceRef() {
        return Objects.requireNonNull(sourceRef, () -> "SourceRef is not present for instance " + this);
    }

    public boolean isView() {
        return sourceRef != null;
    }

    public MappingKey getMappingKey() {
        if (tryGetId() instanceof ViewId viewId) {
            return viewId.getMappingKey();
        } else
            throw new InternalException("Not a view instance");
    }

    public @Nullable Id tryGetId() {
        return useOldId ? oldId : id;
    }

    public Id getCurrentId() {
        return Objects.requireNonNull(id);
    }

    public @Nullable Id tryGetCurrentId() {
        return id;
    }

    public Id getId() {
        return requireNonNull(tryGetId(), () -> Instances.getInstancePath(this) + " id not initialized yet");
    }

    public String getStringId() {
        return NncUtils.get(tryGetId(), Id::toString);
    }

    public Object getMappedEntity() {
        return mappedEntity;
    }

    public void setMappedEntity(Object mappedEntity) {
        this.mappedEntity = mappedEntity;
    }

    public boolean isRemoved() {
        return removed;
    }

    public boolean isNew() {
        return _new;
    }

    public void setRemoved() {
        if (removed)
            throw new InternalException(String.format("Instance %s is already removed", this));
        removed = true;
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
            setParent(parentRef.parent().resolve(), parentRef.field());
    }

    public void setParent(DurableInstance parent, @Nullable Field parentField) {
        ensureLoaded();
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
        setParentInternal(parent, parentField, id == null || !id.isRoot());
    }

    public void setParentInternal(@Nullable InstanceParentRef parentRef) {
        if (parentRef != null)
            setParentInternal(parentRef.parent().resolve(), parentRef.field(), true);
        else
            setParentInternal(null, null, true);
    }

    public void setEphemeral() {
        if (ephemeral)
            return;
        if (!_new)
            throw new IllegalStateException("Can not make a persisted instance ephemeral");
        forEachDescendant(instance -> instance.ephemeral = true);
    }

    public void setParentInternal(@Nullable DurableInstance parent, @Nullable Field parentField, boolean setRoot) {
        if (parent == this.parent && parentField == this.parentField)
            return;
        if (parent != null) {
            this.parent = parent;
            if (parent instanceof ClassInstance) {
                this.parentField = requireNonNull(parentField);
//                assert parentField.isChild() : "Invalid parent field: " + parentField;
            } else if (parent instanceof ArrayInstance parentArray) {
                NncUtils.requireNull(parentField);
                assert parentArray.isChildArray();
                this.parentField = null;
            } else
                throw new IllegalArgumentException("Invalid parent: " + parent);
            if (setRoot)
                root = parent.getRoot();
            if (!pendingChild)
                aggregateRoot = parent;
            if (parent.isEphemeral() && !ephemeral) {
                forEachDescendant(instance -> instance.ephemeral = true);
            }
        } else {
            this.parent = null;
            this.parentField = null;
            if (setRoot)
                aggregateRoot = root = this;
        }
    }

    public void ensureLoaded() {
        if (!loaded && load != null) {
            load.accept(this);
            loaded = true;
        }
    }

    public boolean isInitialized() {
        return _new || loaded;
    }

    public boolean isRoot() {
        return !isValue() && getRoot() == this;
    }

    void setLoaded(boolean fromCache) {
        if (loaded)
            throw new InternalException(String.format("Instance %d is already loaded", getTreeId()));
        loaded = true;
        _new = false;
        setLoadedFromCache(fromCache);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setContext(IInstanceContext context) {
        this.context = context;
    }

    public IInstanceContext getContext() {
        return context;
    }

    public DurableInstance getRoot() {
        if (root == this)
            return this;
        else
            return root = root.getRoot();
    }

    public boolean canMerge() {
        return !pendingChild && parent != null && isRoot();
    }

    public boolean canExtract() {
        return !isRoot() && parent != null && parentField != null && !parentField.isChild();
    }

    @Nullable
    public Long tryGetTreeId() {
        return id != null ? id.tryGetTreeId() : null;
    }

    public long getTreeId() {
        var treeId = tryGetTreeId();
        if (treeId != null)
            return treeId;
        else
            throw new NullPointerException("Instance id not initialized yet");
    }

    public boolean idEquals(long id) {
        return Objects.equals(this.tryGetTreeId(), id);
    }

    public boolean isIdInitialized() {
        return id != null && !id.isTemporary();
    }

    public void initId(Id id) {
        if (isIdInitialized())
            throw new InternalException("id already initialized");
        if (isArray()) {
            if (!RegionConstants.isArrayId(id))
                throw new InternalException("Invalid array id");
        }
        this.id = id;
    }

    void ensureMutable() {
        if (isLoadedFromCache())
            throw new IllegalStateException(String.format("Instance %s is immutable", this));
    }

    public long getVersion() {
        ensureLoaded();
        return version;
    }

    public long getSyncVersion() {
        ensureLoaded();
        return syncVersion;
    }

    public Set<DurableInstance> getChildren() {
        ensureLoaded();
        return Set.of();
    }

    public Tree toTree() {
        NncUtils.requireTrue(isRoot());
        return new Tree(getTreeId(), getVersion(), nextNodeId, InstanceOutput.toBytes(this));
    }

    public void writeRecord(InstanceOutput output) {
        if (isValue())
            output.write(WireTypes.VALUE);
        else {
            if (oldId != null) {
                output.write(WireTypes.MIGRATING_RECORD);
                output.writeLong(oldId.getTreeId());
                output.writeLong(oldId.getNodeId());
                output.writeBoolean(useOldId);
            } else
                output.write(WireTypes.RECORD);
            output.writeLong(id.getNodeId());
        }
        getType().write(output);
        writeBody(output);
    }

    protected abstract void writeBody(InstanceOutput output);

    @Override
    public void writeTo(InstanceOutput output) {
        output.write(TreeTags.DEFAULT);
        // !!! IMPORTANT: Version must starts at the second byte. @see org.metavm.entity.ContextDifference.incVersion !!!
        output.writeLong(getVersion());
        output.writeLong(getTreeId());
        output.writeLong(getNextNodeId());
        if (isSeparateChild()) {
            output.writeBoolean(true);
            output.writeId(Objects.requireNonNull(getParent()).getId());
            output.writeId(Objects.requireNonNull(getParentField()).getId());
        } else
            output.writeBoolean(false);
        writeRecord(output);
    }

    public abstract void readFrom(InstanceInput input);

    boolean isModified() {
        return modified;
    }

    void setModified() {
        ensureMutable();
        this.modified = true;
    }

    public @Nullable DurableInstance getParent() {
        ensureLoaded();
        return parent;
    }

    public boolean isChildOf(DurableInstance instance, @Nullable Field parentField) {
        return !isRoot() && parent == instance && this.parentField == parentField;
    }

    @Nullable
    public Field getParentField() {
        ensureLoaded();
        return parentField;
    }

    public @Nullable InstanceParentRef getParentRef() {
        ensureLoaded();
        return parent == null ? null : new InstanceParentRef(parent.getReference(), parentField);
    }


    public String getDescription() {
        if (id != null && getTitle().equals(id.toString())) {
            return getType().getName() + "/" + getTitle();
        } else {
            if (!getTitle().isEmpty()) {
                return getType().getName() + "/" + getTitle() + "/" + id;
            } else {
                return getType().getName() + "/" + id;
            }
        }
    }

    public abstract Set<DurableInstance> getRefInstances();

    public void incVersion() {
        ensureLoaded();
        version++;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setSyncVersion(long syncVersion) {
        this.syncVersion = syncVersion;
    }

    public Object getNativeObject() {
        return nativeObject;
    }

    public void setNativeObject(Object nativeObject) {
        this.nativeObject = nativeObject;
    }

    @Override
    public final String toString() {
        return getType().getTypeDesc() + "-" + getTitle();
    }


    void insertAfter(DurableInstance instance) {
        var next = this.next;
        instance.next = next;
        if (next != null)
            next.prev = instance;
        this.next = instance;
        instance.prev = this;
    }


    public Object toSearchConditionValue() {
        return id.getTreeId();
    }


    void unlink() {
        DurableInstance next = this.next, prev = this.prev;
        if (prev != null)
            prev.next = next;
        if (next != null)
            next.prev = prev;
        this.next = this.prev = null;
    }

    @Nullable
    DurableInstance getNext() {
        return next;
    }

    @Nullable
    DurableInstance getPrev() {
        return prev;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public long nextNodeId() {
        return nextNodeId++;
    }

    public long getNextNodeId() {
        return nextNodeId;
    }

    public void setNextNodeId(long nextNodeId) {
        this.nextNodeId = nextNodeId;
    }

    boolean isMarked() {
        return marked;
    }

    void setMarked(boolean marked) {
        this.marked = marked;
    }

    boolean isViewSaved() {
        return viewSaved;
    }

    void setViewSaved() {
        this.viewSaved = true;
    }

    public boolean setChangeNotified() {
        if (changeNotified)
            return false;
        changeNotified = true;
        return true;
    }

    public boolean setRemovalNotified() {
        if (removalNotified)
            return false;
        removalNotified = true;
        return true;
    }

    public boolean setAfterContextInitIdsNotified() {
        if (afterContextInitIdsNotified)
            return false;
        afterContextInitIdsNotified = true;
        return true;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isValue() {
        return getType().isValue();
    }

    public Id getOldId() {
        return Objects.requireNonNull(oldId);
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

    void clearOldId() {
        assert oldId != null;
        this.oldId = null;
    }

    public boolean isSeparateChild() {
        ensureLoaded();
        return isRoot() && parent != null;
    }

    public boolean isPendingChild() {
        return pendingChild;
    }

    public DurableInstance getAggregateRoot() {
        if (aggregateRoot == this)
            return this;
        else
            return aggregateRoot = aggregateRoot.getAggregateRoot();
    }

    public void merge() {
        this.oldId = id;
        this.oldRoot = root;
        this.root = Objects.requireNonNull(parent).getRoot();
        this.id = migratedId != null ? migratedId : PhysicalId.of(root.getTreeId(), root.nextNodeId(), getType());
        useOldId = true;
    }

    public void rollbackMerge() {
        migratedId = id;
        id = oldId;
        oldId = null;
        useOldId = false;
        root = Objects.requireNonNull(oldRoot);
        oldRoot = null;
    }

    public void extract(boolean isRoot) {
        this.oldId = id;
        this.id = migratedId;
        useOldId = true;
        if (isRoot) {
            this.root = aggregateRoot = this;
            oldRoot = parent;
            oldParentField = parentField;
            parent = null;
            parentField = null;
        } else {
            this.oldRoot = this.root;
            this.root = aggregateRoot = Objects.requireNonNull(parent);
        }
    }

    public void rollbackExtraction() {
        var isRoot = isRoot();
        migratedId = id;
        id = oldId;
        oldId = null;
        useOldId = false;
        if (isRoot) {
            root = aggregateRoot = parent = Objects.requireNonNull(oldRoot);
            parentField = oldParentField;
            oldParentField = null;
        } else
            root = aggregateRoot = Objects.requireNonNull(oldRoot);
        oldRoot = null;
    }

    public void switchId() {
        useOldId = false;
    }

    public void writeForwardingPointers(InstanceOutput output) {
        output.write(TreeTags.MIGRATED);
        output.writeId(Objects.requireNonNull(oldId));
        output.writeId(id);
    }

    public void setPendingChild(boolean pendingChild) {
        this.pendingChild = pendingChild;
    }

    public InstanceReference getReference() {
        return new InstanceReference(this);
    }

    public abstract boolean isArray();

    public abstract String getTitle();

    public void forEachDescendant(Consumer<DurableInstance> action) {
        action.accept(this);
        forEachChild(c -> c.forEachDescendant(action));
    }

    public void forEachDescendantConditional(Predicate<DurableInstance> action) {
        if (action.test(this))
            forEachChild(c -> c.forEachDescendantConditional(action));
    }

    public abstract void forEachChild(Consumer<DurableInstance> action);

    public abstract void forEachMember(Consumer<DurableInstance> action);

    public abstract void forEachReference(Consumer<InstanceReference> action);

    public abstract void forEachReference(BiConsumer<InstanceReference, Boolean> action);

    public void visitGraph(Predicate<DurableInstance> action) {
        visitGraph(action, r -> true, new IdentitySet<>());
    }

    public void visitGraph(Predicate<DurableInstance> action, Predicate<InstanceReference> predicate) {
        visitGraph(action, predicate, new IdentitySet<>());
    }

    public void visitGraph(Predicate<DurableInstance> action, Predicate<InstanceReference> predicate, IdentitySet<DurableInstance> visited) {
        if (DebugEnv.recordPath)
            DebugEnv.path.clear();
        visitGraph0(action, predicate, visited);
    }

    private void visitGraph0(Predicate<DurableInstance> action, Predicate<InstanceReference> predicate, IdentitySet<DurableInstance> visited) {
        if (DebugEnv.recordPath)
            DebugEnv.path.addLast(this.toString());
        if (visited.add(this) && action.test(this)) {
            forEachReference(r -> {
                if (predicate.test(r))
                    r.resolve().visitGraph0(action, predicate, visited);
            });
        }
        if (DebugEnv.recordPath)
            DebugEnv.path.removeLast();
    }

    protected abstract InstanceParam getParam();

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
                    getType().toExpression(serContext),
                    getType().getName(),
                    getTitle(),
                    Instances.getSourceMappingRefDTO(this.getReference()),
                    param
            );
        }
    }

    public abstract DurableInstance copy();

    public String getQualifiedTitle() {
        return getType().getName() + "-" + getTitle();
    }

    protected abstract void writeTree(TreeWriter treeWriter);

    public String getText() {
        var treeWriter = new TreeWriter();
        writeTree(treeWriter);
        return treeWriter.toString();
    }

    public abstract void accept(DurableInstanceVisitor visitor);

    public Id getMigratedId() {
        return Objects.requireNonNull(migratedId);
    }
}
