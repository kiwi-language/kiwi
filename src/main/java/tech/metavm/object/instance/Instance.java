package tech.metavm.object.instance;

import tech.metavm.entity.IdInitializing;
import tech.metavm.entity.NoProxy;
import tech.metavm.object.instance.persistence.IdentityPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.rest.FieldValueDTO;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class Instance implements IdInitializing {

    private Long id;
    private final Type type;
    private long version;
    private long syncVersion;

    private transient final Set<ReferenceRT> outgoingReferences = new HashSet<>();
    private transient final Set<ReferenceRT> incomingReferences = new HashSet<>();

    public Instance(Type type) {
        this.type = type;
    }

    public Instance(Long id, Type type, long version, long syncVersion) {
        this.type = type;
        this.version = version;
        this.syncVersion = syncVersion;
        if(id != null) {
            initId(id);
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
    public Long getId() {
        return id;
    }

    @Override
    @NoProxy
    public void initId(long id) {
        if(this.id != null) {
            throw new InternalException("id already initialized");
        }
        if(isArray() && !TypeCategory.ARRAY.idRangeContains(id)) {
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
        if(!outgoingReferences.add(reference)) {
            throw new InternalException(reference + " already exists");
        }
    }

    @NoProxy
    public void removeOutgoingReference(ReferenceRT reference) {
        if(!outgoingReferences.remove(reference)) {
            throw new InternalException(reference + " does not exist");
        }
    }

    @NoProxy
    public void addIncomingReference(ReferenceRT reference) {
        if(!incomingReferences.add(reference)) {
            throw new InternalException(reference + " already exists");
        }
    }

    @NoProxy
    public void removeIncomingReference(ReferenceRT reference) {
        if(!incomingReferences.remove(reference)) {
            throw new InternalException(reference + " does not exist");
        }
    }

    @NoProxy
    public Set<ReferenceRT> getIncomingReferences() {
        return Collections.unmodifiableSet(incomingReferences);
    }

    @NoProxy
    public Set<ReferenceRT> getOutgoingReferences() {
        return Collections.unmodifiableSet(outgoingReferences);
    }

    public ReferenceRT getOutgoingReference(Instance target, Field field) {
        return NncUtils.findRequired(outgoingReferences, ref -> ref.target() == target && ref.field() == field);
    }

    protected InstanceDTO toDTO(Object param) {
        return new InstanceDTO(
                id,
                type.getId(),
                type.getName(),
                getTitle(),
                param
        );
    }

    public abstract FieldValueDTO toFieldValueDTO();

    public InstanceDTO toDTO() {
        return toDTO(getParam());
    }

    public abstract String getTitle();

    public String getDescription() {
        return type.getName() + "/" + getTitle();
    }

    protected abstract Object getParam();

    public void incVersion() {
        version++;
    }

}
