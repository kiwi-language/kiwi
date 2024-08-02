package org.metavm.object.instance.core;

import org.metavm.object.type.PrimitiveType;

public abstract class NumberValue extends PrimitiveValue/* implements Comparable<NumberInstance>*/ {

    public NumberValue(PrimitiveType type) {
        super(type);
    }

    public abstract DoubleValue toDouble();

    public abstract double doubleValue();

    public abstract NumberValue negate();

    public abstract NumberValue add(NumberValue that);

    public abstract NumberValue minus(NumberValue that);

    public abstract NumberValue mul(NumberValue that);

    public abstract NumberValue div(NumberValue that);

    public abstract NumberValue mod(NumberValue that);

    public abstract BooleanValue lt(NumberValue that);

    public abstract BooleanValue le(NumberValue that);

    public abstract BooleanValue gt(NumberValue that);

    public abstract BooleanValue ge(NumberValue that);

}
