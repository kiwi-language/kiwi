package tech.metavm.object.instance;

import tech.metavm.entity.IdInitializing;
import tech.metavm.entity.NoProxy;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.meta.Type;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.Set;

public abstract class Instance implements IdInitializing {

    private Long id;
    private final Type type;
    private long version;
    private long syncVersion;

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
    public boolean isPassword() {
        return type.isPassword();
    }

    @NoProxy
    public boolean isArray() {
        return type.isArray();
    }

    @NoProxy
    public final ReferencePO toReferencePO() {
        return new ReferencePO(NncUtils.requireNonNull(getId()));
    }

    public abstract Object toColumnValue(long tenantId, IdentitySet<Instance> visited);

    public abstract boolean isReference();

    public abstract Set<Instance> getRefInstances();

    public abstract InstancePO toPO(long tenantId);

    abstract InstancePO toPO(long tenantId, IdentitySet<Instance> visited);

    public String toStringValue() {
        throw new UnsupportedOperationException();
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

    protected InstanceDTO toDTO(Object param) {
        return new InstanceDTO(
                id,
                type.getId(),
                type.getName(),
                getTitle(),
                param
        );
    }

    public InstanceDTO toDTO() {
        return toDTO(getParam());
    }

    public abstract String getTitle();

    protected abstract Object getParam();

}
