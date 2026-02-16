package org.manul.object.instance.core;

import lombok.extern.slf4j.Slf4j;
import org.manul.object.type.PrimitiveType;
import org.manul.util.Instances;
import org.manul.util.MvOutput;
import org.manul.util.WireTypes;

@Slf4j
public class IntValue extends NumberValue {

    private static final int CACHE_LOW = -128;
    private static final int CACHE_HIGH = 128;
    private static final IntValue[] CACHE = new IntValue[CACHE_HIGH - CACHE_LOW + 1];

    static {
        for (int i = 0; i < CACHE.length; i++) {
            CACHE[i] = new IntValue(i + CACHE_LOW);
        }
    }

    public static IntValue of(int value) {
        if (value >= CACHE_LOW && value <= CACHE_HIGH)
            return CACHE[value - CACHE_LOW];
        return new IntValue(value);
    }

    public static final IntValue zero = CACHE[-CACHE_LOW];

    public static final IntValue one = CACHE[1 - CACHE_LOW];

    public static final IntValue minusOne = CACHE[-1 - CACHE_LOW];

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
