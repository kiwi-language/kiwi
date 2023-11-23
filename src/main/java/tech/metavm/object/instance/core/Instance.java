package tech.metavm.object.instance.core;

import tech.metavm.common.ErrorCode;
import tech.metavm.entity.IdInitializing;
import tech.metavm.entity.NoProxy;
import tech.metavm.entity.SerializeContext;
import tech.metavm.management.RegionManager;
import tech.metavm.object.instance.persistence.IdentityPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceParam;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.rest.dto.InstanceParentRef;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public abstract class Instance implements IdInitializing {

    private transient boolean _new;
    private transient boolean loaded;
    transient boolean loadedFromCache;
    transient boolean cacheEnabled;
    private transient boolean removed;
    private transient IInstanceContext context;
    @Nullable
    private transient Instance prev;
    @Nullable
    private transient Instance next;
    private transient Object mappedEntity;
    private transient boolean modified;

    @Nullable
    private Long id;
    private final Type type;
    private long version;
    private long syncVersion;
    @Nullable
    private Instance parent;
    @Nullable
    private Field parentField;
    private Instance root;

    private transient Long tmpId;

    private transient final Map<ReferenceRT, Integer> outgoingReferences = new HashMap<>();

    private transient Object nativeObject;

    private final @Nullable Consumer<Instance> load;

    public Instance(Type type) {
        this(type, null);
    }

    public Instance(Type type, @Nullable InstanceParentRef parentRef) {
        this(null, type, parentRef, 0L, 0L, null);
    }

    public Instance(@Nullable Long id, Type type,
                    @Nullable InstanceParentRef parentRef,
                    long version, long syncVersion,
                    @Nullable Consumer<Instance> load
    ) {
        this.type = type;
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
                        classParent.setChild(NncUtils.requireNonNull(parentRef.field()), this);
                case ArrayInstance arrayParent -> {
                    NncUtils.requireNull(parentField);
                    arrayParent.addChild(this);
                }
                default -> throw new IllegalStateException("Unexpected value: " + parent);
            }
            this.parent = parentRef.parent();
            this.parentField = parentRef.field();
            root = parent.root;
        }
        else
            root = this;
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
        if(removed)
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
    public void resetParent(@Nullable InstanceParentRef parentRef) {
        if(parentRef != null)
            resetParent(parentRef.parent(), parentRef.field());
        else
            resetParent(null, null);
    }

    @NoProxy
    public void resetParent(@Nullable Instance parent, @Nullable Field parentField) {
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
    public void ensureLoaded() {
        if (!loaded && load != null) {
            load.accept(this);
            loaded = true;
        }
    }

    @NoProxy
    public Type getType() {
        return type;
    }

    @NoProxy
    public boolean isValue() {
        return type.isValue();
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
        if(loaded)
            throw new InternalException(String.format("Instance %d is already loaded", getIdRequired()));
        loaded = true;
        _new = false;
        loadedFromCache = fromCache;
    }

    @NoProxy
    public boolean isLoaded() {
        return loaded;
    }

    @NoProxy
    public boolean isNull() {
        return false;
    }

    @NoProxy
    public boolean isNotNull() {
        return !isNull();
    }

    @NoProxy
    public boolean isPassword() {
        return type.isPassword();
    }

    public Instance convert(Type type) {
        throw new BusinessException(ErrorCode.CONVERSION_FAILED, getQualifiedTitle(), type.getName());
    }

    public void setContext(IInstanceContext context) {
        this.context = context;
    }

    public IInstanceContext getContext() {
        return context;
    }

    @NoProxy
    public boolean isArray() {
        return this instanceof ArrayInstance;
    }

    @NoProxy
    public Long getTmpId() {
        return tmpId;
    }

    @NoProxy
    public void setTmpId(Long tmpId) {
        this.tmpId = tmpId;
    }

    @NoProxy
    public final IdentityPO toIdentityPO() {
        return new IdentityPO(NncUtils.requireNonNull(getId()));
    }

    @NoProxy
    public boolean isPrimitive() {
        return false;
    }

    @NoProxy
    public boolean isNotPrimitive() {
        return !isPrimitive();
    }

    public abstract boolean isReference();

    public abstract Set<Instance> getRefInstances();

    public abstract InstancePO toPO(long tenantId);

    abstract InstancePO toPO(long tenantId, IdentitySet<Instance> visited);

    public String toStringValue() {
        throw new UnsupportedOperationException();
    }

    public Instance getRoot() {
        if(root == this)
            return this;
        else
            return root = root.getRoot();
    }

    @NoProxy
    public Object toSearchConditionValue() {
        return NncUtils.requireNonNull(id);
    }

    @Override
    @NoProxy
    @Nullable
    public Long getId() {
        return id;
    }

    @NoProxy
    public Long getIdRequired() {
        if(id != null)
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

    @Override
    @NoProxy
    public void initId(long id) {
        if (this.id != null)
            throw new InternalException("id already initialized");
        if (isArray()) {
            if (!RegionManager.isArrayId(id))
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

    public boolean isChild(Instance instance) {
        return false;
    }

    public Set<Instance> getChildren() {
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

    public abstract void writeTo(InstanceOutput output, boolean includeChildren);

    public abstract void readFrom(InstanceInput input);

    protected InstanceDTO toDTO(InstanceParam param) {
        ensureLoaded();
        try (var context = SerializeContext.enter()) {
            return new InstanceDTO(
                    NncUtils.orElse(id, () -> 0L),
                    context.getRef(getType()),
                    type.getName(),
                    getTitle(),
                    param
            );
        }
    }

    @NoProxy
    boolean isModified() {
        return modified;
    }

    @NoProxy
    void setModified() {
        ensureMutable();
        this.modified = true;
    }

    public abstract FieldValue toFieldValueDTO();

    public InstanceDTO toDTO() {
        return toDTO(getParam());
    }

    public abstract String getTitle();

    public String getQualifiedTitle() {
        return getType().getName() + "-" + getTitle();
    }

    public String getDescription() {
        if (id != null && getTitle().equals(id.toString())) {
            return type.getName() + "/" + getTitle();
        } else {
            if (!getTitle().isEmpty()) {
                return type.getName() + "/" + getTitle() + "/" + id;
            } else {
                return type.getName() + "/" + id;
            }
        }
    }

    @Nullable
    public Instance getParent() {
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

    @Override
    @NoProxy
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    @NoProxy
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    protected abstract InstanceParam getParam();

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
    public abstract void accept(InstanceVisitor visitor);

    public abstract void acceptReferences(InstanceVisitor visitor);

    public abstract void acceptChildren(InstanceVisitor visitor);

    @NoProxy
    @Override
    public final String toString() {
        if (this.isInitialized()) {
            return getType().getName() + "-" + getTitle();
        } else {
            return String.format("Uninitialized instance, id: %s", id);
        }
    }

    void insertAfter(Instance instance) {
        var next = this.next;
        instance.next = next;
        if(next != null)
            next.prev = instance;
        this.next = instance;
        instance.prev = this;
    }

    void unlink() {
        Instance next = this.next, prev = this.prev;
        if(prev != null)
            prev.next = next;
        if(next != null)
            next.prev = prev;
        this.next = this.prev = null;
    }

    @Nullable Instance getNext() {
        return next;
    }

    @Nullable Instance getPrev() {
        return prev;
    }
}
