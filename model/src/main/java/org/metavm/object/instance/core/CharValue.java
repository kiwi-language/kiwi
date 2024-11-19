package org.metavm.object.instance.core;

import org.metavm.object.type.PrimitiveType;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;
import org.metavm.util.Instances;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

public class CharValue extends PrimitiveValue {

    private final char value;

    public CharValue(char value, PrimitiveType type) {
        super(type);
        this.value = value;
    }

    public Character getValue() {
        return value;
    }

    @Override
    public Value convert(Type type) {
        if (type.isAssignableFrom(PrimitiveType.doubleType))
            return toDouble();
        else
            return super.convert(type);
    }

    public DoubleValue toDouble() {
        return new DoubleValue(value, Types.getDoubleType());
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

}
