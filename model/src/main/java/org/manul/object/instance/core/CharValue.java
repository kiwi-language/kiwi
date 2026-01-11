package org.manul.object.instance.core;

import org.manul.object.type.PrimitiveType;
import org.manul.util.Instances;
import org.manul.util.MvOutput;
import org.manul.util.WireTypes;

public class CharValue extends PrimitiveValue {

    public final char value;

    public CharValue(char value) {
        this.value = value;
    }

    public Character getValue() {
        return value;
    }

    @Override
    public PrimitiveType getValueType() {
        return PrimitiveType.charType;
    }

    public DoubleValue toDouble() {
        return new DoubleValue(value);
    }

    public BooleanValue gt(CharValue that) {
        return Instances.createBoolean(value > that.value);
    }

    public BooleanValue ge(CharValue that) {
        return Instances.createBoolean(value >= that.value);
    }

    public BooleanValue lt(CharValue that) {
        return Instances.createBoolean(value < that.value);
    }

    public BooleanValue le(CharValue that) {
        return Instances.createBoolean(value <= that.value);
    }

    @Override
    public String getTitle() {
        return Long.toString(value);
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.CHAR);
        output.writeChar(value);
    }

    @Override
    public <R> R accept(ValueVisitor<R> visitor) {
        return visitor.visitCharValue(this);
    }

    @Override
    public Value toStackValue() {
        return new IntValue(value);
    }
}
