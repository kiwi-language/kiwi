package tech.metavm.object.instance.core;

import tech.metavm.object.type.PrimitiveType;

public abstract class NumberInstance extends PrimitiveInstance {

    public NumberInstance(PrimitiveType type) {
        super(type);
    }

    public abstract DoubleInstance toDouble();

    public abstract NumberInstance negate();

    public abstract NumberInstance add(NumberInstance that);

    public abstract NumberInstance minus(NumberInstance that);

    public abstract NumberInstance mul(NumberInstance that);

    public abstract NumberInstance div(NumberInstance that);

    public abstract NumberInstance mod(NumberInstance that);

    public abstract BooleanInstance lt(NumberInstance that);

    public abstract BooleanInstance le(NumberInstance that);

    public abstract BooleanInstance gt(NumberInstance that);

    public abstract BooleanInstance ge(NumberInstance that);

}
