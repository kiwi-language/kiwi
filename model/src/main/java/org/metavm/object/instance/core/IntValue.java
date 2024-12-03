package org.metavm.object.instance.core;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.type.PrimitiveType;
import org.metavm.object.type.Types;
import org.metavm.util.Instances;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

@Slf4j
public class IntValue extends NumberValue {

    public final int value;

    public IntValue(int value, PrimitiveType type) {
        super(type);
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public DoubleValue toDouble() {
        return new DoubleValue(value, Types.getDoubleType());
    }

    @Override
    public double doubleValue() {
        return value;
    }

    public IntValue inc(int inc) {
        return new IntValue(value + inc, getType());
    }

    public IntValue dec(int dec) {
        return new IntValue(value - dec, getType());
    }

    public IntValue minus(IntValue that) {
        return new IntValue(value - that.value, getType());
    }

    public IntValue add(IntValue that) {
        return new IntValue(value + that.value, getType());
    }

    public IntValue bitAnd(IntValue that) {
        return new IntValue(value & that.value, getType());
    }

    public IntValue bitXor(IntValue that) {
        return new IntValue(value ^ that.value, getType());
    }

    public IntValue bitOr(IntValue that) {
        return new IntValue(value | that.value, getType());
    }

    public IntValue bitNot() {
        return new IntValue(~value, getType());
    }

    public IntValue mul(IntValue that) {
        return new IntValue(value * that.value, getType());
    }

    public IntValue div(IntValue that) {
        return new IntValue(value / that.value, getType());
    }

    public IntValue leftShift(IntValue that) {
        return new IntValue(value << that.value, getType());
    }

    public IntValue rightShift(IntValue that) {
        return new IntValue(value >> that.value, getType());
    }

    public IntValue unsignedRightShift(IntValue that) {
        return new IntValue(value >>> that.value, getType());
    }

    public IntValue mod(IntValue that) {
        return new IntValue(value % that.value, getType());
    }

    public BooleanValue gt(IntValue that) {
        return Instances.createBoolean(value > that.value);
    }

    public BooleanValue ge(IntValue that) {
        return Instances.createBoolean(value >= that.value);
    }

    public BooleanValue lt(IntValue that) {
        return Instances.createBoolean(value < that.value);
    }

    public BooleanValue le(IntValue that) {
        return Instances.createBoolean(value <= that.value);
    }

    @Override
    public String getTitle() {
        return Integer.toString(value);
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.INT);
        output.writeInt(value);
    }

    @Override
    public <R> R accept(ValueVisitor<R> visitor) {
        return visitor.visitIntValue(this);
    }

    @Override
    public IntValue negate() {
        return new IntValue(-value, getType());
    }

    @Override
    public NumberValue add(NumberValue that) {
        if (that instanceof IntValue thatInt)
            return add(thatInt);
        else
            return that.add(this);
    }

    @Override
    public NumberValue sub(NumberValue that) {
        if (that instanceof IntValue thatInt)
            return minus(thatInt);
        else
            return that.sub(this);
    }

    @Override
    public NumberValue mul(NumberValue that) {
        if (that instanceof IntValue thatInt)
            return mul(thatInt);
        else
            return that.mul(this);
    }

    @Override
    public NumberValue div(NumberValue that) {
        if (that instanceof IntValue thatInt)
            return div(thatInt);
        else
            return that.div(this);
    }

    @Override
    public NumberValue rem(NumberValue that) {
        if (that instanceof IntValue thatInt)
            return mod(thatInt);
        else
            return that.rem(this);
    }

    @Override
    public BooleanValue lt(NumberValue that) {
        if (that instanceof IntValue thatInt)
            return lt(thatInt);
        else
            return that.lt(this);
    }

    @Override
    public BooleanValue le(NumberValue that) {
        if (that instanceof IntValue thatInt)
            return le(thatInt);
        else
            return that.le(this);
    }

    @Override
    public BooleanValue gt(NumberValue that) {
        if (that instanceof IntValue thatInt)
            return gt(thatInt);
        else
            return that.gt(this);
    }

    @Override
    public BooleanValue ge(NumberValue that) {
        if (that instanceof IntValue thatInt)
            return ge(thatInt);
        else
            return that.ge(this);
    }

}
