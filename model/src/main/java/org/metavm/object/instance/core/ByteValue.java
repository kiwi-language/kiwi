package org.metavm.object.instance.core;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.type.PrimitiveType;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

@Slf4j
public class ByteValue extends NumberValue {

    public final byte value;

    public ByteValue(byte value) {
        this.value = value;
    }

    public Byte getValue() {
        return value;
    }

    @Override
    public PrimitiveType getValueType() {
        return PrimitiveType.byteType;
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
        output.write(WireTypes.BYTE);
        output.write(value);
    }

    @Override
    public <R> R accept(ValueVisitor<R> visitor) {
        return visitor.visitByteValue(this);
    }

    @Override
    public ByteValue negate() {
        return new ByteValue((byte) -value);
    }

    public ByteValue minus(ByteValue that) {
        return new ByteValue((byte) (value - that.value));
    }

    public ByteValue mod(ByteValue that) {
        return new ByteValue((byte) (value % that.value));
    }

    @Override
    public NumberValue add(NumberValue that) {
        if (that instanceof ByteValue thatShort)
            return add(thatShort);
        else
            return that.add(this);
    }

    @Override
    public NumberValue sub(NumberValue that) {
        if (that instanceof ByteValue thatShort)
            return minus(thatShort);
        else
            return that.sub(this);
    }

    @Override
    public NumberValue mul(NumberValue that) {
        if (that instanceof ByteValue thatShort)
            return mul(thatShort);
        else
            return that.mul(this);
    }

    @Override
    public NumberValue div(NumberValue that) {
        if (that instanceof ByteValue thatShort)
            return div(thatShort);
        else
            return that.div(this);
    }

    @Override
    public NumberValue rem(NumberValue that) {
        if (that instanceof ByteValue thatShort)
            return mod(thatShort);
        else
            return that.rem(this);
    }

    @Override
    public BooleanValue lt(NumberValue that) {
        if (that instanceof ByteValue thatShort)
            return lt(thatShort);
        else
            return that.lt(this);
    }

    @Override
    public BooleanValue le(NumberValue that) {
        if (that instanceof ByteValue thatShort)
            return le(thatShort);
        else
            return that.le(this);
    }

    @Override
    public BooleanValue gt(NumberValue that) {
        if (that instanceof ByteValue thatShort)
            return gt(thatShort);
        else
            return that.gt(this);
    }

    @Override
    public BooleanValue ge(NumberValue that) {
        if (that instanceof ByteValue thatShort)
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
        return value + "b";
    }

}
