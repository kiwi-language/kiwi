package org.metavm.object.instance.core;

import org.metavm.object.instance.rest.PrimitiveFieldValue;
import org.metavm.object.type.PrimitiveType;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

public class BooleanValue extends PrimitiveValue {

    public static final BooleanValue true_ = new BooleanValue(true);

    public static final BooleanValue false_ = new BooleanValue(false);

    public final boolean value;

    public BooleanValue(boolean value) {
        this.value = value;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public PrimitiveType getValueType() {
        return PrimitiveType.booleanType;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.BOOLEAN);
        output.writeBoolean(value);
    }

    public boolean isTrue() {
        return value;
    }

    public boolean isFalse() {
        return !value;
    }

    public BooleanValue not() {
        return new BooleanValue(!value);
    }

    public BooleanValue and(BooleanValue that) {
        return new BooleanValue(value && that.value);
    }

    public BooleanValue or(BooleanValue that) {
        return new BooleanValue(value || that.value);
    }

    @Override
    public String getTitle() {
        return value ? "true" : "false";
    }

    @Override
    public <R> R accept(ValueVisitor<R> visitor) {
        return visitor.visitBooleanValue(this);
    }

    @Override
    public PrimitiveFieldValue toFieldValueDTO() {
        return value ? PrimitiveFieldValue.TRUE : PrimitiveFieldValue.FALSE;
    }

    @Override
    public Value toStackValue() {
        return value ? IntValue.one : IntValue.zero;
    }
}
