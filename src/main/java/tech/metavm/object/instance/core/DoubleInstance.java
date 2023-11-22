package tech.metavm.object.instance.core;

import tech.metavm.object.type.PrimitiveType;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.WireTypes;

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
    public int getWireType() {
        return WireTypes.DOUBLE;
    }

    @Override
    public Double toColumnValue() {
        return value;
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

    public BooleanInstance isGreaterThan(DoubleInstance that) {
        return InstanceUtils.createBoolean(value > that.value);
    }

    public BooleanInstance isGreaterThanOrEqualTo(DoubleInstance that) {
        return InstanceUtils.createBoolean(value >= that.value);
    }

    public BooleanInstance isLessThan(DoubleInstance that) {
        return InstanceUtils.createBoolean(value < that.value);
    }

    public BooleanInstance isLessThanOrEqualTo(DoubleInstance that) {
        return InstanceUtils.createBoolean(value <= that.value);
    }

    public DoubleInstance mod(DoubleInstance that) {
        return new DoubleInstance(value % that.value, getType());
    }

    @Override
    public void writeTo(InstanceOutput output, boolean includeChildren) {
        output.writeDouble(value);
    }

    @Override
    public String getTitle() {
        return DF.format(value);
    }

    @Override
    public void accept(InstanceVisitor visitor) {
        visitor.visitDoubleInstance(this);
    }

}
