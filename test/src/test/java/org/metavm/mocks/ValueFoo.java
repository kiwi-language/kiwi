package org.metavm.mocks;

import org.metavm.entity.Entity;
import org.metavm.object.type.ClassType;

public class ValueFoo extends Entity {

    private final ClassType classType;

    public ValueFoo(ClassType classType) {
        this.classType = classType;
    }

    public ClassType getClassType() {
        return classType;
    }
}
