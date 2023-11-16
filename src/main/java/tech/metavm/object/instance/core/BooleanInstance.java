package tech.metavm.object.instance.core;

import tech.metavm.object.type.PrimitiveType;

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
    public Object toColumnValue() {
        return value;
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
    public void accept(InstanceVisitor visitor) {
        visitor.visitBooleanInstance(this);
    }

}
