package tech.metavm.object.instance;

import tech.metavm.object.meta.PrimitiveType;

import java.text.DecimalFormat;

public class DoubleInstance extends PrimitiveInstance {

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

    public DoubleInstance inc(int inc) {
        return new DoubleInstance(value + inc, getType());
    }

    public DoubleInstance dec(int dec) {
        return new DoubleInstance(value - dec, getType());
    }

    public DoubleInstance subtract(DoubleInstance that) {
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

    @Override
    public String getTitle() {
        return DF.format(value);
    }

    @Override
    public String toString() {
        return "DoubleInstance " + value + ":" + getType().getName();
    }

}
