package tech.metavm.object.instance.core;

import tech.metavm.object.instance.rest.PrimitiveFieldValue;
import tech.metavm.object.type.PrimitiveType;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.WireTypes;

public class BooleanInstance extends PrimitiveInstance {

    private final boolean value;

    public BooleanInstance(boolean value, PrimitiveType type) {
        super(type);
        this.value = value;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public int getWireType() {
        return WireTypes.BOOLEAN;
    }

    public boolean isTrue() {
        return value;
    }

    public boolean isFalse() {
        return !value;
    }

    public BooleanInstance not() {
        return new BooleanInstance(!value, getType());
    }

    public BooleanInstance and(BooleanInstance that) {
        return new BooleanInstance(value && that.value, getType());
    }

    public BooleanInstance or(BooleanInstance that) {
        return new BooleanInstance(value || that.value, getType());
    }

    @Override
    public String getTitle() {
        return value ? "是" : "否";
    }

    @Override
    public void writeTo(InstanceOutput output, boolean includeChildren) {
        output.writeBoolean(value);
    }

    @Override
    public <R> R accept(InstanceVisitor<R> visitor) {
        return visitor.visitBooleanInstance(this);
    }

    @Override
    public PrimitiveFieldValue toFieldValueDTO() {
        return value ? PrimitiveFieldValue.TRUE : PrimitiveFieldValue.FALSE;
    }
}
