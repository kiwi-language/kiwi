package tech.metavm.object.instance;

import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.meta.PrimitiveType;
import tech.metavm.util.IdentitySet;

import java.util.Objects;
import java.util.Set;

public abstract class PrimitiveInstance extends Instance {

    public PrimitiveInstance(PrimitiveType type) {
        super(type);
    }

    public abstract Object getValue();

    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    protected Object getParam() {
        return null;
    }

    @Override
    public Set<Instance> getRefInstances() {
        return Set.of();
    }

    @Override
    public Object toColumnValue(long tenantId, IdentitySet<Instance> visited) {
        return getValue();
    }

    @Override
    public Object toSearchConditionValue() {
        return getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrimitiveInstance that = (PrimitiveInstance) o;
        return Objects.equals(getValue(), that.getValue()) && Objects.equals(getType(), that.getType());
    }

    @Override
    public PrimitiveType getType() {
        return (PrimitiveType) super.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue(), getType());
    }

    @Override
    public InstancePO toPO(long tenantId) {
        return null;
    }

    @Override
    InstancePO toPO(long tenantId, IdentitySet<Instance> visited) {
        return null;
    }

}
