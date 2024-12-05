package org.metavm.object.instance.core;

import org.metavm.object.type.PrimitiveType;
import org.metavm.util.Instances;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

import java.text.DecimalFormat;

public class FloatValue extends NumberValue {

    private static final DecimalFormat DF = new DecimalFormat("0.##");

    public final float value;

    public FloatValue(float value) {
        this.value = value;
    }

    @Override
    public Float getValue() {
        return value;
    }

    @Override
    public PrimitiveType getType() {
        return PrimitiveType.floatType;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.FLOAT);
        output.writeFloat(value);
    }

    public FloatValue inc(int inc) {
        return new FloatValue(value + inc);
    }

    public FloatValue dec(int dec) {
        return new FloatValue(value - dec);
    }

    public FloatValue minus(DoubleValue that) {
        return new FloatValue(value - (float) that.value);
    }

    public FloatValue add(FloatValue that) {
        return new FloatValue(value + that.value);
    }

    public FloatValue mul(FloatValue that) {
        return new FloatValue(value * that.value);
    }

    public FloatValue div(FloatValue that) {
        return new FloatValue(value / that.value);
    }

    public BooleanValue gt(FloatValue that) {
        return Instances.createBoolean(value > that.value);
    }

    public BooleanValue ge(FloatValue that) {
        return Instances.createBoolean(value >= that.value);
    }

    public BooleanValue lt(FloatValue that) {
        return Instances.createBoolean(value < that.value);
    }

    public BooleanValue le(FloatValue that) {
        return Instances.createBoolean(value <= that.value);
    }

    public FloatValue mod(DoubleValue that) {
        return new FloatValue(value % (float) that.value);
    }

    @Override
    public String getTitle() {
        return DF.format(value);
    }

    @Override
    public <R> R accept(ValueVisitor<R> visitor) {
        return visitor.visitFloatValue(this);
    }

    @Override
    public FloatValue negate() {
        return new FloatValue(-value);
    }

    @Override
    public DoubleValue toDouble() {
        return new DoubleValue(value);
    }

    @Override
    public NumberValue add(NumberValue that) {
        return add(that.toDouble());
    }

    public FloatValue rem(NumberValue that) {
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
    public FloatValue div(NumberValue that) {
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

    @Override
    protected void writeTree(TreeWriter treeWriter) {
        treeWriter.write(value + "D");
    }

    @Override
    public String toString() {
        return value + "D";
    }

    //    @Override
//    public int compareTo(@NotNull NumberInstance o) {
//        return Double.compare(value, o.doubleValue());
//    }
}
