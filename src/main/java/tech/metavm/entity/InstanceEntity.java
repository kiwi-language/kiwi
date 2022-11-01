package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceContext;
import tech.metavm.object.instance.InstanceListener;
import tech.metavm.object.instance.persistence.*;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class InstanceEntity extends Entity {

    protected transient final long version;
    protected transient final Instance instance;
    protected final Type type;

    public InstanceEntity(Instance instance) {
        super(instance.getId(), instance.getContext().getEntityContext());
        this.instance = instance;
        this.type = instance.getType();
        this.version = instance.getVersion();
        instance.addIdInitCallback(this::initId);
    }

    public InstanceEntity(Type type) {
        this(type.newInstance());
    }

    public VersionPO nextVersion() {
        return new VersionPO(getTenantId(), id, version + 1);
    }

    public final void saveToContext() {
        instance.update(toInstanceDTO());
    }

    final void removeFromContext() {
        NncUtils.invoke(getId(), getInstanceContext()::remove);
    }

    public Type getType() {
        return type;
    }

    protected abstract InstanceDTO toInstanceDTO();

    protected InstanceContext getInstanceContext() {
        return getContext().getInstanceContext();
    }

}
