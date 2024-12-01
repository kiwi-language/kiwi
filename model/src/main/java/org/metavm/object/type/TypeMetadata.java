package org.metavm.object.type;

import org.metavm.flow.MethodRef;

public interface TypeMetadata {

    Type getType(int index);

    default ClassType getClasType(int index) {
        return (ClassType) getType(index);
    }

    default FunctionType getFunctionType(int index) {
        return (FunctionType) getType(index);
    }

    MethodRef getMethodRef(int index);

    Object[] getValues();

    int size();

}
