package org.metavm.object.instance.core;

public abstract class NumberValue extends PrimitiveValue/* implements Comparable<NumberInstance>*/ {

    public NumberValue() {
    }

    public abstract DoubleValue toDouble();

    public abstract double doubleValue();

    public abstract NumberValue negate();

    public abstract NumberValue add(NumberValue that);

    public abstract NumberValue sub(NumberValue that);

    public abstract NumberValue mul(NumberValue that);

    public abstract NumberValue div(NumberValue that);

    public abstract NumberValue rem(NumberValue that);

    public abstract BooleanValue lt(NumberValue that);

    public abstract BooleanValue le(NumberValue that);

    public abstract BooleanValue gt(NumberValue that);

    public abstract BooleanValue ge(NumberValue that);

}
