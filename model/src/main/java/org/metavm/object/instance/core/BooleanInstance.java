package org.metavm.object.instance.core;

import org.metavm.object.instance.rest.PrimitiveFieldValue;
import org.metavm.object.type.PrimitiveType;
import org.metavm.util.InstanceOutput;
import org.metavm.util.WireTypes;

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
    public void write(InstanceOutput output) {
        output.write(WireTypes.BOOLEAN);
        output.writeBoolean(value);
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
        return value ? "true" : "false";
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
