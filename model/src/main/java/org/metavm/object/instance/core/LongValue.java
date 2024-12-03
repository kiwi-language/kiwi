package org.metavm.object.instance.core;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.type.PrimitiveType;
import org.metavm.object.type.Types;
import org.metavm.util.Instances;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

@Slf4j
public class LongValue extends NumberValue {

    public final long value;

    public LongValue(long value, PrimitiveType type) {
        super(type);
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    public DoubleValue toDouble() {
        return new DoubleValue(value, Types.getDoubleType());
    }

    @Override
    public double doubleValue() {
        return value;
    }

    public LongValue inc(long inc) {
        return new LongValue(value + inc, getType());
    }

    public LongValue dec(long dec) {
        return new LongValue(value - dec, getType());
    }

    public LongValue minus(LongValue that) {
        return new LongValue(value - that.value, getType());
    }

    public LongValue add(LongValue that) {
        return new LongValue(value + that.value, getType());
    }

    public LongValue bitAnd(LongValue that) {
        return new LongValue(value & that.value, getType());
    }

    public LongValue bitXor(LongValue that) {
        return new LongValue(value ^ that.value, getType());
    }

    public LongValue bitOr(LongValue that) {
        return new LongValue(value | that.value, getType());
    }

    public LongValue bitNot() {
        return new LongValue(~value, getType());
    }

    public LongValue mul(LongValue that) {
        return new LongValue(value * that.value, getType());
    }

    public LongValue div(LongValue that) {
        return new LongValue(value / that.value, getType());
    }

    public LongValue leftShift(LongValue that) {
        return new LongValue(value << that.value, getType());
    }

    public LongValue rightShift(LongValue that) {
        return new LongValue(value >> that.value, getType());
    }

    public LongValue unsignedRightShift(LongValue that) {
        return new LongValue(value >>> that.value, getType());
    }

    public LongValue mod(LongValue that) {
        return new LongValue(value % that.value, getType());
    }

    public BooleanValue gt(LongValue that) {
        return Instances.createBoolean(value > that.value);
    }

    public BooleanValue ge(LongValue that) {
        return Instances.createBoolean(value >= that.value);
    }

    public BooleanValue lt(LongValue that) {
        return Instances.createBoolean(value < that.value);
    }

    public BooleanValue le(LongValue that) {
        return Instances.createBoolean(value <= that.value);
    }

    @Override
    public String getTitle() {
        return Long.toString(value);
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.LONG);
        output.writeLong(value);
    }

    @Override
    public <R> R accept(ValueVisitor<R> visitor) {
        return visitor.visitLongValue(this);
    }

    @Override
    public LongValue negate() {
        return new LongValue(-value, getType());
    }

    @Override
    public NumberValue add(NumberValue that) {
        if (that instanceof LongValue thatLong)
            return add(thatLong);
        else
            return that.add(this);
    }

    @Override
    public NumberValue sub(NumberValue that) {
        if (that instanceof LongValue thatLong)
            return minus(thatLong);
        else
            return that.sub(this);
    }

    @Override
    public NumberValue mul(NumberValue that) {
        if (that instanceof LongValue thatLong)
            return mul(thatLong);
        else
            return that.mul(this);
    }

    @Override
    public NumberValue div(NumberValue that) {
        if (that instanceof LongValue thatLong)
            return div(thatLong);
        else
            return that.div(this);
    }

    @Override
    public NumberValue rem(NumberValue that) {
        if (that instanceof LongValue thatLong)
            return mod(thatLong);
        else
            return that.rem(this);
    }

    @Override
    public BooleanValue lt(NumberValue that) {
        if (that instanceof LongValue thatLong)
            return lt(thatLong);
        else
            return that.lt(this);
    }

    @Override
    public BooleanValue le(NumberValue that) {
        if (that instanceof LongValue thatLong)
            return le(thatLong);
        else
            return that.le(this);
    }

    @Override
    public BooleanValue gt(NumberValue that) {
        if (that instanceof LongValue thatLong)
            return gt(thatLong);
        else
            return that.gt(this);
    }

    @Override
    public BooleanValue ge(NumberValue that) {
        if (that instanceof LongValue thatLong)
            return ge(thatLong);
        else
            return that.ge(this);
    }

    @Override
    protected void writeTree(TreeWriter treeWriter) {
        treeWriter.write(value + "L");
    }

    @Override
    public String toString() {
        return value + "L";
    }

    //    @Override
//    public int compareTo(@NotNull NumberInstance o) {
//        return switch (o) {
//            case LongInstance l -> Long.compare(value, l.value);
//            default -> o.compareTo(this);
//        };
//    }

}
