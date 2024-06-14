package org.metavm.object.instance.core;

import org.metavm.object.type.PrimitiveType;
import org.metavm.util.InstanceOutput;
import org.metavm.util.Instances;
import org.metavm.util.WireTypes;

import java.text.DecimalFormat;

public class DoubleInstance extends NumberInstance {

    private static final DecimalFormat DF = new DecimalFormat("0.##");

    private final double value;

    public DoubleInstance(double value, PrimitiveType type) {
        super(type);
        this.value = value;
    }

    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(WireTypes.DOUBLE);
        output.writeDouble(value);
    }

    public DoubleInstance inc(int inc) {
        return new DoubleInstance(value + inc, getType());
    }

    public DoubleInstance dec(int dec) {
        return new DoubleInstance(value - dec, getType());
    }

    public DoubleInstance minus(DoubleInstance that) {
        return new DoubleInstance(value - that.value, getType());
    }

    public DoubleInstance add(DoubleInstance that) {
        return new DoubleInstance(value + that.value, getType());
    }

    public DoubleInstance mul(DoubleInstance that) {
        return new DoubleInstance(value * that.value, getType());
    }

    public DoubleInstance div(DoubleInstance that) {
        return new DoubleInstance(value / that.value, getType());
    }

    public BooleanInstance gt(DoubleInstance that) {
        return Instances.createBoolean(value > that.value);
    }

    public BooleanInstance ge(DoubleInstance that) {
        return Instances.createBoolean(value >= that.value);
    }

    public BooleanInstance lt(DoubleInstance that) {
        return Instances.createBoolean(value < that.value);
    }

    public BooleanInstance le(DoubleInstance that) {
        return Instances.createBoolean(value <= that.value);
    }

    public DoubleInstance mod(DoubleInstance that) {
        return new DoubleInstance(value % that.value, getType());
    }

    @Override
    public String getTitle() {
        return DF.format(value);
    }

    @Override
    public <R> R accept(InstanceVisitor<R> visitor) {
        return visitor.visitDoubleInstance(this);
    }

    @Override
    public DoubleInstance negate() {
        return new DoubleInstance(-value, getType());
    }

    @Override
    public DoubleInstance toDouble() {
        return this;
    }

    @Override
    public NumberInstance add(NumberInstance that) {
        return add(that.toDouble());
    }

    public DoubleInstance mod(NumberInstance that) {
        return mod(that.toDouble());
    }

    @Override
    public NumberInstance minus(NumberInstance that) {
        return minus(that.toDouble());
    }

    @Override
    public NumberInstance mul(NumberInstance that) {
        return mul(that.toDouble());
    }

    @Override
    public DoubleInstance div(NumberInstance that) {
        return div(that.toDouble());
    }

    @Override
    public BooleanInstance lt(NumberInstance that) {
        return lt(that.toDouble());
    }

    @Override
    public BooleanInstance le(NumberInstance that) {
        return le(that.toDouble());
    }

    @Override
    public BooleanInstance gt(NumberInstance that) {
        return gt(that.toDouble());
    }

    @Override
    public BooleanInstance ge(NumberInstance that) {
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
