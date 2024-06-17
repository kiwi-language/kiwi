package org.metavm.object.instance.core;

import org.metavm.object.type.PrimitiveKind;
import org.metavm.object.type.PrimitiveType;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;
import org.metavm.util.InstanceOutput;
import org.metavm.util.Instances;
import org.metavm.util.WireTypes;

public class LongInstance extends NumberInstance {

    private final long value;

    public LongInstance(long value, PrimitiveType type) {
        super(type);
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    @Override
    public Instance convert(Type type) {
        if (type instanceof PrimitiveType primitiveType && primitiveType.getKind() == PrimitiveKind.DOUBLE)
            return toDouble();
        else
            return super.convert(type);
    }

    public DoubleInstance toDouble() {
        return new DoubleInstance(value, Types.getDoubleType());
    }

    @Override
    public double doubleValue() {
        return value;
    }

    public LongInstance inc(long inc) {
        return new LongInstance(value + inc, getType());
    }

    public LongInstance dec(long dec) {
        return new LongInstance(value - dec, getType());
    }

    public LongInstance minus(LongInstance that) {
        return new LongInstance(value - that.value, getType());
    }

    public LongInstance add(LongInstance that) {
        return new LongInstance(value + that.value, getType());
    }

    public LongInstance mul(LongInstance that) {
        return new LongInstance(value * that.value, getType());
    }

    public LongInstance div(LongInstance that) {
        return new LongInstance(value / that.value, getType());
    }

    public LongInstance leftShift(LongInstance that) {
        return new LongInstance(value << that.value, getType());
    }

    public LongInstance rightShift(LongInstance that) {
        return new LongInstance(value >> that.value, getType());
    }

    public LongInstance unsignedRightShift(LongInstance that) {
        return new LongInstance(value >>> that.value, getType());
    }

    public LongInstance mod(LongInstance that) {
        return new LongInstance(value % that.value, getType());
    }

    public BooleanInstance gt(LongInstance that) {
        return Instances.createBoolean(value > that.value);
    }

    public BooleanInstance ge(LongInstance that) {
        return Instances.createBoolean(value >= that.value);
    }

    public BooleanInstance lt(LongInstance that) {
        return Instances.createBoolean(value < that.value);
    }

    public BooleanInstance le(LongInstance that) {
        return Instances.createBoolean(value <= that.value);
    }

    @Override
    public String getTitle() {
        return Long.toString(value);
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(WireTypes.LONG);
        output.writeLong(value);
    }

    @Override
    public <R> R accept(InstanceVisitor<R> visitor) {
        return visitor.visitLongInstance(this);
    }

    @Override
    public LongInstance negate() {
        return new LongInstance(-value, getType());
    }

    @Override
    public NumberInstance add(NumberInstance that) {
        if (that instanceof LongInstance thatLong)
            return add(thatLong);
        else
            return that.add(this);
    }

    @Override
    public NumberInstance minus(NumberInstance that) {
        if (that instanceof LongInstance thatLong)
            return minus(thatLong);
        else
            return that.minus(this);
    }

    @Override
    public NumberInstance mul(NumberInstance that) {
        if (that instanceof LongInstance thatLong)
            return mul(thatLong);
        else
            return that.mul(this);
    }

    @Override
    public NumberInstance div(NumberInstance that) {
        if (that instanceof LongInstance thatLong)
            return div(thatLong);
        else
            return that.div(this);
    }

    @Override
    public NumberInstance mod(NumberInstance that) {
        if (that instanceof LongInstance thatLong)
            return mod(thatLong);
        else
            return that.mod(this);
    }

    @Override
    public BooleanInstance lt(NumberInstance that) {
        if (that instanceof LongInstance thatLong)
            return lt(thatLong);
        else
            return that.lt(this);
    }

    @Override
    public BooleanInstance le(NumberInstance that) {
        if (that instanceof LongInstance thatLong)
            return le(thatLong);
        else
            return that.le(this);
    }

    @Override
    public BooleanInstance gt(NumberInstance that) {
        if (that instanceof LongInstance thatLong)
            return gt(thatLong);
        else
            return that.gt(this);
    }

    @Override
    public BooleanInstance ge(NumberInstance that) {
        if (that instanceof LongInstance thatLong)
            return ge(thatLong);
        else
            return that.ge(this);
    }

//    @Override
//    public int compareTo(@NotNull NumberInstance o) {
//        return switch (o) {
//            case LongInstance l -> Long.compare(value, l.value);
//            default -> o.compareTo(this);
//        };
//    }

}
