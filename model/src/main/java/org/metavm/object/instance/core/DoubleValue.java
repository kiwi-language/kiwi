package org.metavm.object.instance.core;

import org.metavm.object.type.PrimitiveType;
import org.metavm.util.Instances;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

import java.text.DecimalFormat;

public class DoubleValue extends NumberValue {

    private static final DecimalFormat DF = new DecimalFormat("0.##");

    private final double value;

    public DoubleValue(double value, PrimitiveType type) {
        super(type);
        this.value = value;
    }

    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.DOUBLE);
        output.writeDouble(value);
    }

    public DoubleValue inc(int inc) {
        return new DoubleValue(value + inc, getType());
    }

    public DoubleValue dec(int dec) {
        return new DoubleValue(value - dec, getType());
    }

    public DoubleValue minus(DoubleValue that) {
        return new DoubleValue(value - that.value, getType());
    }

    public DoubleValue add(DoubleValue that) {
        return new DoubleValue(value + that.value, getType());
    }

    public DoubleValue mul(DoubleValue that) {
        return new DoubleValue(value * that.value, getType());
    }

    public DoubleValue div(DoubleValue that) {
        return new DoubleValue(value / that.value, getType());
    }

    public BooleanValue gt(DoubleValue that) {
        return Instances.createBoolean(value > that.value);
    }

    public BooleanValue ge(DoubleValue that) {
        return Instances.createBoolean(value >= that.value);
    }

    public BooleanValue lt(DoubleValue that) {
        return Instances.createBoolean(value < that.value);
    }

    public BooleanValue le(DoubleValue that) {
        return Instances.createBoolean(value <= that.value);
    }

    public DoubleValue mod(DoubleValue that) {
        return new DoubleValue(value % that.value, getType());
    }

    @Override
    public String getTitle() {
        return DF.format(value);
    }

    @Override
    public <R> R accept(ValueVisitor<R> visitor) {
        return visitor.visitDoubleValue(this);
    }

    @Override
    public DoubleValue negate() {
        return new DoubleValue(-value, getType());
    }

    @Override
    public DoubleValue toDouble() {
        return this;
    }

    @Override
    public NumberValue add(NumberValue that) {
        return add(that.toDouble());
    }

    public DoubleValue rem(NumberValue that) {
        return mod(that.toDouble());
    }

    @Override
    public NumberValue sub(NumberValue that) {
        return minus(that.toDouble());
    }

    @Override
    public NumberValue mul(NumberValue that) {
        return mul(that.toDouble());
    }

    @Override
    public DoubleValue div(NumberValue that) {
        return div(that.toDouble());
    }

    @Override
    public BooleanValue lt(NumberValue that) {
        return lt(that.toDouble());
    }

    @Override
    public BooleanValue le(NumberValue that) {
        return le(that.toDouble());
    }

    @Override
    public BooleanValue gt(NumberValue that) {
        return gt(that.toDouble());
    }

    @Override
    public BooleanValue ge(NumberValue that) {
        return ge(that.toDouble());
    }

    @Override
    public double doubleValue() {
        return value;
    }

//    @Override
//    public int compareTo(@NotNull NumberInstance o) {
//        return Double.compare(value, o.doubleValue());
//    }
}
