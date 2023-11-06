package tech.metavm.object.instance.core;

import tech.metavm.entity.IdInitializing;
import tech.metavm.entity.NoProxy;
import tech.metavm.entity.SerializeContext;
import tech.metavm.object.instance.persistence.IdentityPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceParamDTO;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.object.meta.rest.dto.InstanceParentRef;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class Instance implements IdInitializing {

    @Nullable
    private Long id;
    private final Type type;
    private long version;
    private long syncVersion;
    @Nullable
    private Instance parent;
    @Nullable
    private Field parentField;

    private transient Long tmpId;

    private transient final Map<ReferenceRT, Integer> outgoingReferences = new HashMap<>();
    private transient final Map<ReferenceRT, Integer> incomingReferences = new HashMap<>();

    private transient Object nativeObject;

    public Instance(Type type) {
        this(type, null);
    }

    public Instance(Type type, @Nullable InstanceParentRef parentRef) {
        this(null, type, parentRef, 0L, 0L);
    }

    public Instance(@Nullable Long id, Type type,
                    @Nullable InstanceParentRef parentRef,
                    long version, long syncVersion) {
        this.type = type;
        this.version = version;
        this.syncVersion = syncVersion;
        if (id != null) {
            initId(id);
        }
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
        }
    }

    @NoProxy
    public void reloadParent(@Nullable InstanceParentRef parentRef) {
        if (parentRef != null) {
            this.parent = parentRef.parent();
            if (parent instanceof ClassInstance) {
                this.parentField = NncUtils.requireNonNull(parentRef.field());
            } else {
                NncUtils.requireNull(parentRef.field());
                this.parentField = null;
            }
        } else {
            this.parent = null;
            this.parentField = null;
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
    public boolean isNull() {
        return type.isNull();
    }

    @NoProxy
    public boolean isNotNull() {
        return !isNull();
    }

    @NoProxy
    public boolean isPassword() {
        return type.isPassword();
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

    public abstract Object toColumnValue(long tenantId, IdentitySet<Instance> visited);

    public abstract boolean isReference();

    public abstract Set<Instance> getRefInstances();

    public abstract InstancePO toPO(long tenantId);

    abstract InstancePO toPO(long tenantId, IdentitySet<Instance> visited);

    public String toStringValue() {
        throw new UnsupportedOperationException();
    }

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
        return NncUtils.requireNonNull(getId());
    }

    @Override
    @NoProxy
    public void initId(long id) {
        if (this.id != null) {
            throw new InternalException("id already initialized");
        }
        if (isArray() && NncUtils.noneMatch(TypeCategory.arrayCategories(), category -> category.idRangeContains(id))) {
            throw new InternalException("Invalid id for array instance");
        }
        this.id = id;
    }

    @Override
    @NoProxy
    public void clearId() {
        this.id = null;
    }

    public long getVersion() {
        return version;
    }

    public long getSyncVersion() {
        return syncVersion;
    }

    public boolean isChild(Instance instance) {
        return false;
    }

    public Set<Instance> getChildren() {
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
    public void addIncomingReference(ReferenceRT reference) {
        incomingReferences.compute(reference, (k, c) -> c == null ? 1 : c + 1);
    }

    @NoProxy
    public void removeIncomingReference(ReferenceRT reference) {
        incomingReferences.compute(reference, (k, c) -> c == null || c <= 1 ? null : c - 1);
    }

    @NoProxy
    public Set<ReferenceRT> getIncomingReferences() {
        return Collections.unmodifiableSet(incomingReferences.keySet());
    }

    @NoProxy
    public Set<ReferenceRT> getOutgoingReferences() {
        return Collections.unmodifiableSet(outgoingReferences.keySet());
    }

    public ReferenceRT getOutgoingReference(Instance target, Field field) {
        return NncUtils.findRequired(outgoingReferences.keySet(),
                ref -> ref.target() == target && ref.field() == field);
    }

    protected InstanceDTO toDTO(InstanceParamDTO param) {
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

    public abstract FieldValue toFieldValueDTO();

    public InstanceDTO toDTO() {
        return toDTO(getParam());
    }

    public abstract String getTitle();

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
        return parent;
    }

    @Nullable
    public Field getParentField() {
        return parentField;
    }

    public InstanceParentRef getParentRef() {
        return new InstanceParentRef(parent, parentField);
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

    protected abstract InstanceParamDTO getParam();

    public void incVersion() {
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
    public String toString() {
        return getType().getName() + "-" + getTitle();
    }
}
