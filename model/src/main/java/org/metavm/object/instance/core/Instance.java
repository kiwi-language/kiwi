package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.Tree;
import org.metavm.entity.TreeTags;
import org.metavm.entity.natives.NativeBase;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.object.instance.rest.InstanceDTO;
import org.metavm.object.instance.rest.InstanceParam;
import org.metavm.object.type.Field;
import org.metavm.object.type.Type;
import org.metavm.object.type.rest.dto.InstanceParentRef;
import org.metavm.system.RegionConstants;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;
import java.util.function.*;

import static java.util.Objects.requireNonNull;

public abstract class Instance implements Message {

    public static final Logger logger = LoggerFactory.getLogger(Instance.class);

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    private Type type;
    private transient boolean marked;
    private transient final boolean _new;
    private transient boolean loaded;
    private transient boolean loadedFromCache;
    private transient boolean removed;
    private transient boolean modified;
    private transient boolean ephemeral;
    private transient boolean changeNotified;
    private transient boolean removalNotified;
    private transient boolean directlyModified;

    transient IInstanceContext context;
    private transient boolean afterContextInitIdsNotified;
    private @Nullable
    transient Instance prev;
    private @Nullable
    transient Instance next;
    private transient Object mappedEntity;

    private Id id;

    private long version;
    private long syncVersion;

    private @Nullable Instance parent;
    private @Nullable Field parentField;
    private @NotNull Instance root;
    private @Nullable Instance oldRoot;
    private @Nullable Field oldParentField;
    private @Nullable Id oldId;
    private @Nullable Id relocatedId;
    private @NotNull Instance aggregateRoot;
    private boolean pendingChild;
    private boolean useOldId;
    private boolean removing;

    private transient Long tmpId;

    private transient NativeBase nativeObject;

    private final @Nullable Consumer<Instance> load;

    private int seq;

    private long nextNodeId = 1;

    public Instance(Type type) {
        this(null, type, 0L, 0L, false, null);
    }

    public Instance(@Nullable Id id, Type type, long version, long syncVersion, boolean ephemeral, @Nullable Consumer<Instance> load) {
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

    public InstancePO toPO(long appId) {
        return new InstancePO(
                appId,
                getTreeId(),
                InstanceOutput.toBytes(this),
                getVersion(),
                getSyncVersion(),
                getNextNodeId()
        );
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

    public void setParent(Instance parent, @Nullable Field parentField) {
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

    public void setParentInternal(@Nullable Instance parent, @Nullable Field parentField, boolean setRoot) {
        if (parent == this.parent && parentField == this.parentField)
            return;
        if (parent != null) {
            this.parent = parent;
            if (parent instanceof ClassInstance) {
                this.parentField = parentField;
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

    public void clearParent() {
        this.parent = null;
        this.parentField = null;
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
        return !isInlineValue() && getRoot() == this;
    }

    public boolean isReferencedByParent() {
        if(parent != null) {
            var ref = new Object() {
                boolean referenced;
            };
            parent.forEachChild(c -> {
                if(c == this)
                    ref.referenced = true;
            });
            return ref.referenced;
        }
        else
            return false;
    }

    void setLoaded(boolean fromCache) {
        if (loaded)
            throw new InternalException(String.format("Instance %d is already loaded", getTreeId()));
        loaded = true;
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

    public Instance getRoot() {
        if (root == this)
            return this;
        else
            return root = root.getRoot();
    }

    public boolean canMerge() {
        return !pendingChild && parent != null && isRoot();
    }

    public boolean canExtract() {
        return !isValue() && !isRoot() && parent != null && parentField != null && !parentField.isChild();
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

    public boolean idEquals(Id id) {
        var thisId = tryGetId();
        return thisId != null && thisId.equals(id);
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

    public Set<Instance> getChildren() {
        ensureLoaded();
        return Set.of();
    }

    public Tree toTree() {
        NncUtils.requireTrue(isRoot());
        return new Tree(getTreeId(), getVersion(), nextNodeId, InstanceOutput.toBytes(this));
    }

    public void writeRecord(InstanceOutput output) {
        writeHead(output);
        writeBody(output);
    }

    protected void writeHead(InstanceOutput output) {
        if (isInlineValue())
            output.write(WireTypes.VALUE_INSTANCE);
        else {
            if (oldId != null) {
                output.write(WireTypes.RELOCATING_INSTANCE);
                output.writeLong(oldId.getTreeId());
                output.writeLong(oldId.getNodeId());
                output.writeBoolean(useOldId);
            }
            else if(removing)
                output.write(WireTypes.REMOVING_INSTANCE);
            else
                output.write(WireTypes.INSTANCE);
            output.writeLong(id.getNodeId());
        }
        getType().write(output);
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

    public void readRecord(InstanceInput input) {
        setLoaded(input.isLoadedFromCache());
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
        ensureLoaded();
        return parent;
    }

    public @Nullable Instance getParent(int index) {
        var v = parent;
        for (int i = 0; i < index; i++) {
            v = requireNonNull(v).parent;
        }
        return v;
    }

    public boolean isChildOf(Instance instance, @Nullable Field parentField) {
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

    public abstract Set<Instance> getRefInstances();

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

    public NativeBase getNativeObject() {
        return nativeObject;
    }

    public void setNativeObject(NativeBase nativeObject) {
        this.nativeObject = nativeObject;
    }

    @Override
    public final String toString() {
        return getType().getTypeDesc() + "-" + getTitle();
    }


    void insertAfter(Instance instance) {
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
        Instance next = this.next, prev = this.prev;
        if (prev != null)
            prev.next = next;
        if (next != null)
            next.prev = prev;
        this.next = this.prev = null;
    }

    @Nullable
    Instance getNext() {
        return next;
    }

    @Nullable
    Instance getPrev() {
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

    public boolean isInlineValue() {
        return id == null && isValue();
    }

    public boolean isDetachedValue() {
        return id != null && isValue();
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

    public boolean isUseOldId() {
        return useOldId;
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

    public Instance getAggregateRoot() {
        if (aggregateRoot == this)
            return this;
        else
            return aggregateRoot = aggregateRoot.getAggregateRoot();
    }

    public void merge() {
        this.oldId = id;
        this.oldRoot = root;
        this.root = Objects.requireNonNull(parent).getRoot();
        this.id = relocatedId != null ? relocatedId : PhysicalId.of(root.getTreeId(), root.nextNodeId(), getType());
        useOldId = true;
    }

    public void rollbackMerge() {
        relocatedId = id;
        id = oldId;
        oldId = null;
        useOldId = false;
        root = Objects.requireNonNull(oldRoot);
        oldRoot = null;
    }

    public void extract(boolean isRoot) {
        this.oldId = id;
        this.id = relocatedId;
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
        relocatedId = id;
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
        output.write(TreeTags.RELOCATED);
        output.writeId(Objects.requireNonNull(oldId));
        output.writeId(id);
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

    public void forEachDescendant(Consumer<Instance> action) {
        action.accept(this);
        forEachChild(c -> c.forEachDescendant(action));
    }

    public void forEachDescendantConditional(Predicate<Instance> action) {
        if (action.test(this))
            forEachChild(c -> c.forEachDescendantConditional(action));
    }

    public abstract void forEachChild(Consumer<Instance> action);

    public abstract void forEachMember(Consumer<Instance> action);

    public abstract void forEachReference(Consumer<Reference> action);

    public abstract void forEachReference(BiConsumer<Reference, Boolean> action);

    public abstract void forEachReference(TriConsumer<Reference, Boolean, Type> action);

    public void transformReference(Function<Reference, Reference> function) {
        transformReference((r, isChild, type) -> function.apply(r));
    }

    public void transformReference(BiFunction<Reference, Boolean, Reference> function) {
        transformReference((r, isChild, type) -> function.apply(r, isChild));
    }

    public abstract void transformReference(TriFunction<Reference, Boolean, Type, Reference> function);

    public void visitGraph(Predicate<Instance> action) {
        visitGraph(action, r -> true, new IdentitySet<>());
    }

    public void visitGraph(Predicate<Instance> action, Predicate<Reference> predicate) {
        visitGraph(action, predicate, new IdentitySet<>());
    }

    public void visitGraph(Predicate<Instance> action, Predicate<Reference> predicate, IdentitySet<Instance> visited) {
        if (DebugEnv.recordPath)
            DebugEnv.path.clear();
        visitGraph0(action, predicate, visited);
    }

    private void visitGraph0(Predicate<Instance> action, Predicate<Reference> predicate, IdentitySet<Instance> visited) {
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
                    param
            );
        }
    }

    public abstract Instance copy();

    public String getQualifiedTitle() {
        return getType().getName() + "-" + getTitle();
    }

    protected abstract void writeTree(TreeWriter treeWriter);

    public String getText() {
        var treeWriter = new TreeWriter();
        writeTree(treeWriter);
        return treeWriter.toString();
    }

    public abstract void accept(InstanceVisitor visitor);

    public Id getRelocatedId() {
        return Objects.requireNonNull(relocatedId);
    }

    public boolean isRemoving() {
        return removing;
    }

    public void setRemoving(boolean removing) {
        this.removing = removing;
    }

    public boolean isDirectlyModified() {
        return directlyModified;
    }

    public void setDirectlyModified(boolean directlyModified) {
        this.directlyModified = directlyModified;
    }

}
