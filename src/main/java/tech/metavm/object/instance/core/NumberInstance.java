package tech.metavm.object.instance.core;

import tech.metavm.object.type.PrimitiveType;

public abstract class NumberInstance extends PrimitiveInstance {

    public NumberInstance(PrimitiveType type) {
        super(type);
    }

    public abstract NumberInstance negate();

}
