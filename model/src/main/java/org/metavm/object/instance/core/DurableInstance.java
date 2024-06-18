package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.NoProxy;
import org.metavm.entity.Tree;
import org.metavm.object.type.Field;
import org.metavm.object.type.Type;
import org.metavm.object.type.rest.dto.InstanceParentRef;
import org.metavm.object.view.rest.dto.MappingKey;
import org.metavm.system.RegionConstants;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public abstract class DurableInstance extends Instance {

    public static final Logger logger = LoggerFactory.getLogger(DurableInstance.class);

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

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
    @Nullable
    private transient DurableInstance prev;
    @Nullable
    private transient DurableInstance next;
    private transient Object mappedEntity;

    private Id id;

    private long version;
    private long syncVersion;

    @Nullable
    private DurableInstance parent;
    @Nullable
    private Field parentField;
    private @NotNull DurableInstance root = this;

    private transient Long tmpId;

    private transient final Map<ReferenceRT, Integer> outgoingReferences = new HashMap<>();

    private transient Object nativeObject;

    private final @Nullable Consumer<DurableInstance> load;

    private transient @Nullable SourceRef sourceRef;

    private int seq;

    private long nextNodeId = 1;

    public DurableInstance(Type type) {
        this(null, type, 0L, 0L, false, null);
    }

    public DurableInstance(@Nullable Id id, Type type, long version, long syncVersion, boolean ephemeral, @Nullable Consumer<DurableInstance> load) {
        super(type);
        this.version = version;
        this.syncVersion = syncVersion;
        this.load = load;
        this.ephemeral = ephemeral;
        if (id != null) {
            initId(id);
            _new = id.tryGetTreeId() == null;
        } else
            _new = true;
    }

    public boolean isDurable() {
        return !isEphemeral();
    }

    @Override
    public boolean isEphemeral() {
        return ephemeral || getType().isEphemeral();
    }

    @Override
    public boolean shouldSkipWrite() {
        return isInitialized() && isEphemeral();
    }

    public @Nullable DurableInstance tryGetSource() {
        return NncUtils.get(sourceRef, SourceRef::source);
    }

    public @NotNull DurableInstance getSource() {
        return Objects.requireNonNull(tryGetSource());
    }

    public void setSourceRef(@Nullable SourceRef sourceRef) {
        this.sourceRef = sourceRef;
    }

    public SourceRef getSourceRef() {
        return Objects.requireNonNull(sourceRef);
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

    //    @Override
    @NoProxy
    public @Nullable Id tryGetId() {
        return id;
    }

    public Id getId() {
        return requireNonNull(id, () -> Instances.getInstancePath(this) + " id not initialized yet");
    }

    public String getStringId() {
        return NncUtils.get(id, Id::toString);
    }

    public Object getMappedEntity() {
        return mappedEntity;
    }

    public void setMappedEntity(Object mappedEntity) {
        this.mappedEntity = mappedEntity;
    }

    @NoProxy
    public boolean isRemoved() {
        return removed;
    }

    @NoProxy
    public boolean isNew() {
        return _new;
    }

    @NoProxy
    public void setRemoved() {
        if (removed)
            throw new InternalException(String.format("Instance %s is already removed", this));
        removed = true;
    }

    @NoProxy
    public boolean isPersisted() {
        return !isNew();
    }

    @NoProxy
    public boolean isLoadedFromCache() {
        return loadedFromCache;
    }

    void setLoadedFromCache(boolean loadedFromCache) {
        this.loadedFromCache = loadedFromCache;
    }

    public void setParentRef(@Nullable InstanceParentRef parentRef) {
        if (parentRef != null)
            setParent(parentRef.parent(), parentRef.field());
    }

    @NoProxy
    public void setParent(DurableInstance parent, @Nullable Field parentField) {
        ensureLoaded();
        if (this.parent != null) {
            if (this.parent == parent && Objects.equals(this.parentField, parentField))
                return;
            throw new InternalException("Can not change parent of " + Instances.getInstanceDesc(this)
                    + ", current parent: " + Instances.getInstancePath(this.parent)
                    + ", current parentField: " + this.parentField
                    + ", new parent: " + Instances.getInstancePath(parent)
                    + ", new parentField: " + parentField
            );
        }
        setParentInternal(parent, parentField);
    }

    @NoProxy
    public void setParentInternal(@Nullable InstanceParentRef parentRef) {
        if (parentRef != null)
            setParentInternal(parentRef.parent(), parentRef.field());
        else
            setParentInternal(null, null);
    }

    @NoProxy
    public void setParentInternal(@Nullable DurableInstance parent, @Nullable Field parentField) {
        if (parent == this.parent && parentField == this.parentField)
            return;
        if (parent != null) {
            this.parent = parent;
            if (parent instanceof ClassInstance parentClassInst) {
                this.parentField = requireNonNull(parentField);
                assert parentField.isChild() : "Invalid parent field: " + parentField;
//                parentClassInst.setOrInitField(parentField, this);
            } else if (parent instanceof ArrayInstance parentArray) {
                NncUtils.requireNull(parentField);
                assert parentArray.isChildArray();
                this.parentField = null;
//                parentArray.addElement(this);
            } else
                throw new InternalException("Invalid parent: " + parent);
            root = parent.root;
            if (parent.isEphemeral() && !ephemeral) {
                accept(new StructuralVisitor() {
                    @Override
                    public Void visitDurableInstance(DurableInstance instance) {
                        instance.ephemeral = true;
                        return super.visitDurableInstance(instance);
                    }
                });
            }
        } else {
            this.parent = null;
            this.parentField = null;
            root = this;
        }
    }

    @NoProxy
    public void ensureLoaded() {
        if (!loaded && load != null) {
            load.accept(this);
            loaded = true;
        }
    }

    @NoProxy
    public boolean isInitialized() {
        return _new || loaded;
    }

    public boolean isRoot() {
        return root == this;
    }

    @NoProxy
    void setLoaded(boolean fromCache) {
        if (loaded)
            throw new InternalException(String.format("Instance %d is already loaded", getTreeId()));
        loaded = true;
        _new = false;
        setLoadedFromCache(fromCache);
    }

    @NoProxy
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

    @NoProxy
    @Nullable
    public Long tryGetTreeId() {
        return id != null ? id.tryGetTreeId() : null;
    }

    @NoProxy
    public long getTreeId() {
        var treeId = tryGetTreeId();
        if (treeId != null)
            return treeId;
        else
            throw new NullPointerException("Instance id not initialized yet");
    }

    @NoProxy
    public boolean idEquals(long id) {
        return Objects.equals(this.tryGetTreeId(), id);
    }

    @NoProxy
    public boolean isIdInitialized() {
        return id != null && !id.isTemporary();
    }

    @NoProxy
    public void initId(Id id) {
        if (isIdInitialized())
            throw new InternalException("id already initialized");
        if (isArray()) {
            if (!RegionConstants.isArrayId(id))
                throw new InternalException("Invalid array id");
        }
        this.id = id;
    }

    @NoProxy
    void ensureMutable() {
        if (isLoadedFromCache())
            throw new IllegalStateException(String.format("Instance %s is immutable", this));
    }

    @NoProxy
    public void clearId() {
        this.id = null;
    }

    public long getVersion() {
        ensureLoaded();
        return version;
    }

    public long getSyncVersion() {
        ensureLoaded();
        return syncVersion;
    }

    public boolean isChild(DurableInstance instance) {
        return false;
    }

    public Set<DurableInstance> getChildren() {
        ensureLoaded();
        return Set.of();
    }

    @NoProxy
    public void addOutgoingReference(ReferenceRT reference) {
        outgoingReferences.compute(reference, (k, c) -> c == null ? 1 : c + 1);
    }

    @NoProxy
    public void removeOutgoingReference(ReferenceRT reference) {
        outgoingReferences.compute(reference, (k, c) -> c == null || c <= 1 ? null : c - 1);
    }

    @NoProxy
    public Set<ReferenceRT> getOutgoingReferences() {
        return Collections.unmodifiableSet(outgoingReferences.keySet());
    }

    public ReferenceRT getOutgoingReference(Instance target, Field field) {
        ensureLoaded();
        return NncUtils.findRequired(outgoingReferences.keySet(),
                ref -> ref.target() == target && ref.field() == field);
    }

    public Tree toTree() {
        NncUtils.requireTrue(isRoot());
        return new Tree(getTreeId(), getVersion(), nextNodeId, InstanceOutput.toBytes(this));
    }

    @Override
    public void writeRecord(InstanceOutput output) {
        if (isValue())
            output.write(WireTypes.VALUE);
        else {
            output.write(WireTypes.RECORD);
            output.writeLong(getId().getNodeId());
        }
        getType().write(output);
        writeBody(output);
    }

    protected abstract void writeBody(InstanceOutput output);

    @Override
    public void write(InstanceOutput output) {
        output.write(WireTypes.REFERENCE);
        output.writeId(getId());
    }

    public abstract void readFrom(InstanceInput input);

    @NoProxy
    boolean isModified() {
        return modified;
    }

    @NoProxy
    void setModified() {
        ensureMutable();
        this.modified = true;
    }

    public @Nullable DurableInstance getParent() {
        ensureLoaded();
        return parent;
    }

    @Nullable
    public Field getParentField() {
        ensureLoaded();
        return parentField;
    }

    public @Nullable InstanceParentRef getParentRef() {
        ensureLoaded();
        return parent == null ? null : new InstanceParentRef(parent, parentField);
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

    @NoProxy
    public void setVersion(long version) {
        this.version = version;
    }

    @NoProxy
    public void setSyncVersion(long syncVersion) {
        this.syncVersion = syncVersion;
    }

    @NoProxy
    public Object getNativeObject() {
        return nativeObject;
    }

    @NoProxy
    public void setNativeObject(Object nativeObject) {
        this.nativeObject = nativeObject;
    }

    @NoProxy
    @Override
    public final String toString() {
        if (this.isInitialized()) {
            return getType().getName() + "-" + getTitle();
        } else {
            return String.format("Uninitialized instance, id: %s", id);
        }
    }


    void insertAfter(DurableInstance instance) {
        var next = this.next;
        instance.next = next;
        if (next != null)
            next.prev = instance;
        this.next = instance;
        instance.prev = this;
    }


    @NoProxy
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
        super.setType(type);
    }

    @Override
    public boolean isValue() {
        // to-be-initialized instance can not be value
        return isInitialized() && getType().isValue();
    }
}
