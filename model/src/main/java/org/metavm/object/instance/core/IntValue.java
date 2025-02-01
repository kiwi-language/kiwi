package org.metavm.object.instance.core;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.type.PrimitiveType;
import org.metavm.util.Instances;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

@Slf4j
public class IntValue extends NumberValue {

    public static final IntValue zero = new IntValue(0);

    public static final IntValue one = new IntValue(1);

    public static final IntValue minusOne = new IntValue(-1);

    public final int value;

    public IntValue(int value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public PrimitiveType getValueType() {
        return PrimitiveType.intType;
    }

    public DoubleValue toDouble() {
        return new DoubleValue(value);
    }

    @Override
    public double doubleValue() {
        return value;
    }

    public IntValue inc(int inc) {
        return new IntValue(value + inc);
    }

    public IntValue dec(int dec) {
        return new IntValue(value - dec);
    }

    public IntValue minus(IntValue that) {
        return new IntValue(value - that.value);
    }

    public IntValue add(IntValue that) {
        return new IntValue(value + that.value);
    }

    public IntValue bitAnd(IntValue that) {
        return new IntValue(value & that.value);
    }

    public IntValue bitXor(IntValue that) {
        return new IntValue(value ^ that.value);
    }

    public IntValue bitOr(IntValue that) {
        return new IntValue(value | that.value);
    }

    public IntValue bitNot() {
        return new IntValue(~value);
    }

    public IntValue mul(IntValue that) {
        return new IntValue(value * that.value);
    }

    public IntValue div(IntValue that) {
        return new IntValue(value / that.value);
    }

    public IntValue leftShift(IntValue that) {
        return new IntValue(value << that.value);
    }

    public IntValue rightShift(IntValue that) {
        return new IntValue(value >> that.value);
    }

    public IntValue unsignedRightShift(IntValue that) {
        return new IntValue(value >>> that.value);
    }

    public IntValue mod(IntValue that) {
        return new IntValue(value % that.value);
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
        return new IntValue(-value);
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
