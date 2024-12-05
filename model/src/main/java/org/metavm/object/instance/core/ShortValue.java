package org.metavm.object.instance.core;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.type.PrimitiveType;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

@Slf4j
public class ShortValue extends NumberValue {

    public final short value;

    public ShortValue(short value) {
        this.value = value;
    }

    public Short getValue() {
        return value;
    }

    @Override
    public PrimitiveType getType() {
        return PrimitiveType.shortType;
    }

    public DoubleValue toDouble() {
        return new DoubleValue(value);
    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public String getTitle() {
        return Integer.toString(value);
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.SHORT);
        output.writeShort(value);
    }

    @Override
    public <R> R accept(ValueVisitor<R> visitor) {
        return visitor.visitShortValue(this);
    }

    @Override
    public ShortValue negate() {
        return new ShortValue((short) -value);
    }

    public ShortValue minus(ShortValue that) {
        return new ShortValue((short) (value - that.value));
    }

    public ShortValue mod(ShortValue that) {
        return new ShortValue((short) (value % that.value));
    }

    @Override
    public NumberValue add(NumberValue that) {
        if (that instanceof ShortValue thatShort)
            return add(thatShort);
        else
            return that.add(this);
    }

    @Override
    public NumberValue sub(NumberValue that) {
        if (that instanceof ShortValue thatShort)
            return minus(thatShort);
        else
            return that.sub(this);
    }

    @Override
    public NumberValue mul(NumberValue that) {
        if (that instanceof ShortValue thatShort)
            return mul(thatShort);
        else
            return that.mul(this);
    }

    @Override
    public NumberValue div(NumberValue that) {
        if (that instanceof ShortValue thatShort)
            return div(thatShort);
        else
            return that.div(this);
    }

    @Override
    public NumberValue rem(NumberValue that) {
        if (that instanceof ShortValue thatShort)
            return mod(thatShort);
        else
            return that.rem(this);
    }

    @Override
    public BooleanValue lt(NumberValue that) {
        if (that instanceof ShortValue thatShort)
            return lt(thatShort);
        else
            return that.lt(this);
    }

    @Override
    public BooleanValue le(NumberValue that) {
        if (that instanceof ShortValue thatShort)
            return le(thatShort);
        else
            return that.le(this);
    }

    @Override
    public BooleanValue gt(NumberValue that) {
        if (that instanceof ShortValue thatShort)
            return gt(thatShort);
        else
            return that.gt(this);
    }

    @Override
    public BooleanValue ge(NumberValue that) {
        if (that instanceof ShortValue thatShort)
            return ge(thatShort);
        else
            return that.ge(this);
    }

    @Override
    public Value toStackValue() {
        return new IntValue(value);
    }

    @Override
    public String toString() {
        return value + "s";
    }
}
