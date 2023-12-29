package tech.metavm.object.instance.core;

import tech.metavm.entity.IdInitializing;
import tech.metavm.entity.NoProxy;
import tech.metavm.entity.Tree;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.rest.dto.InstanceParentRef;
import tech.metavm.system.RegionConstants;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.function.Consumer;

public abstract class DurableInstance extends Instance implements IdInitializing  {

    private transient boolean _new;
    private transient boolean loaded;
    transient boolean loadedFromCache;
    private transient boolean removed;
    private transient IInstanceContext context;
    @javax.annotation.Nullable
    private transient DurableInstance prev;
    @javax.annotation.Nullable
    private transient DurableInstance next;
    private transient Object mappedEntity;
    private transient boolean modified;

    @javax.annotation.Nullable
    private Long id;

    private long version;
    private long syncVersion;
    @javax.annotation.Nullable
    private DurableInstance parent;
    @javax.annotation.Nullable
    private Field parentField;
    private DurableInstance root;

    private transient Long tmpId;

    private transient final Map<ReferenceRT, Integer> outgoingReferences = new HashMap<>();

    private transient Object nativeObject;

    private final @javax.annotation.Nullable Consumer<DurableInstance> load;


    private transient Instance source;

    public DurableInstance(Type type) {
        this(type, null);
    }

    public DurableInstance(Type type, @Nullable InstanceParentRef parentRef) {
        this(null, type, parentRef, 0L, 0L, null);
    }

    public DurableInstance(@Nullable Long id, Type type, @Nullable InstanceParentRef parentRef, long version, long syncVersion, @Nullable Consumer<DurableInstance> load) {
        super(type);
        this.version = version;
        this.syncVersion = syncVersion;
        this.load = load;
        if (id != null)
            initId(id);
        else
            _new = true;
        if (parentRef != null) {
            switch (parentRef.parent()) {
                case ClassInstance classParent ->
                        classParent.setOrInitField(NncUtils.requireNonNull(parentRef.field()), this);
                case ArrayInstance arrayParent -> {
                    NncUtils.requireNull(parentField);
                    arrayParent.addElement(this);
                }
                default -> throw new IllegalStateException("Unexpected value: " + parent);
            }
            this.parent = parentRef.parent();
            this.parentField = parentRef.field();
            root = parent.root;
        } else
            root = this;

    }

    public Instance getSource() {
        return source;
    }

    public void setSource(Instance source) {
        this.source = source;
    }

    public boolean isView() {
        return source != null;
    }

    public Long getMappingId() {
        if(getInstanceId() instanceof ViewId viewId) {
            return viewId.getMappingId();
        }
        else
            throw new InternalException("Not a view instance");
    }

    @Override
    @NoProxy
    public @javax.annotation.Nullable Id getInstanceId() {
        if(getId() != null)
            return new PhysicalId(getIdRequired());
        else if(getTmpId() != null)
            return new TmpId(getTmpId());
        else
            return null;
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

    @NoProxy
    public void resetParent(@javax.annotation.Nullable InstanceParentRef parentRef) {
        if (parentRef != null)
            resetParent(parentRef.parent(), parentRef.field());
        else
            resetParent(null, null);
    }

    @NoProxy
    public void resetParent(@javax.annotation.Nullable DurableInstance parent, @javax.annotation.Nullable Field parentField) {
        if (parent != null) {
            this.parent = parent;
            if (parent instanceof ClassInstance) {
                this.parentField = NncUtils.requireNonNull(parentField);
            } else {
                NncUtils.requireNull(parentField);
                this.parentField = null;
            }
            root = parent.root;
        } else {
            this.parent = null;
            this.parentField = null;
            root = this;
        }
    }

    @NoProxy
    public void initParent(DurableInstance parent, @javax.annotation.Nullable Field parentField) {
        ensureLoaded();
        if(this.parent != null) {
            if(this.parent == parent && Objects.equals(this.parentField, parentField))
                return;
            throw new InternalException("Can not change parent");
        }
        this.parent = parent;
        this.parentField = parentField;
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
            throw new InternalException(String.format("Instance %d is already loaded", getIdRequired()));
        loaded = true;
        _new = false;
        loadedFromCache = fromCache;
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

    @NoProxy
    public Long getTmpId() {
        return tmpId;
    }

    @NoProxy
    public void setTmpId(Long tmpId) {
        this.tmpId = tmpId;
    }

    public DurableInstance getRoot() {
        if (root == this)
            return this;
        else
            return root = root.getRoot();
    }

    @NoProxy
    @javax.annotation.Nullable
    public Long getId() {
        return id;
    }

    @NoProxy
    public long getIdRequired() {
        if (id != null)
            return id;
        else
            throw new InternalException("Instance id not initialized yet");
    }

    @NoProxy
    public boolean idEquals(long id) {
        return Objects.equals(this.id, id);
    }

    @NoProxy
    public boolean isIdNull() {
        return id == null;
    }

    @NoProxy
    public boolean isIdNotNull() {
        return id != null;
    }

    @NoProxy
    public void initId(long id) {
        if (this.id != null)
            throw new InternalException("id already initialized");
        if (isArray()) {
            if (!RegionConstants.isArrayId(id))
                throw new InternalException("Invalid id for array instance");
        }
        this.id = id;
    }

    @NoProxy
    void ensureMutable() {
        if (loadedFromCache)
            throw new IllegalStateException(String.format("Instance %s is immutable", this));
    }

    @Override
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

    public Tree toTree(boolean withChildren) {
        NncUtils.requireTrue(isRoot());
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout, withChildren);
        output.writeLong(getVersion());
        output.writeValue(this);
        return new Tree(getIdRequired(), getVersion(), bout.toByteArray());
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

    @javax.annotation.Nullable
    public Field getParentField() {
        ensureLoaded();
        return parentField;
    }

    public @javax.annotation.Nullable InstanceParentRef getParentRef() {
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

    @NoProxy
    public void incVersion() {
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
        return NncUtils.requireNonNull(id);
    }


    void unlink() {
        DurableInstance next = this.next, prev = this.prev;
        if (prev != null)
            prev.next = next;
        if (next != null)
            next.prev = prev;
        this.next = this.prev = null;
    }

    @Nullable DurableInstance getNext() {
        return next;
    }

    @Nullable DurableInstance getPrev() {
        return prev;
    }

}
