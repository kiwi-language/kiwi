package tech.metavm.object.instance.core;

import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.rest.PrimitiveFieldValue;
import tech.metavm.object.instance.rest.PrimitiveInstanceParam;
import tech.metavm.object.type.PrimitiveType;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InstanceInput;

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
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    public abstract int getWireType();

    @Override
    protected PrimitiveInstanceParam getParam() {
        return new PrimitiveInstanceParam(
                getType().getKind().code(),
                getValue()
        );
    }

    @Override
    public void acceptReferences(InstanceVisitor visitor) {
    }

    @Override
    public void acceptChildren(InstanceVisitor visitor) {
    }

    @Override
    public Set<Instance> getRefInstances() {
        return Set.of();
    }

    @Override
    public Object toSearchConditionValue() {
        return getValue();
    }

    @Override
    public void readFrom(InstanceInput input) {
        throw new UnsupportedOperationException();
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
    public PrimitiveFieldValue toFieldValueDTO() {
        return new PrimitiveFieldValue(
                getTitle(),
                getType().getKind().code(),
                getValue()
        );
    }

}
