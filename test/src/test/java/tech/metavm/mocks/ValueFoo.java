package tech.metavm.mocks;

import tech.metavm.entity.Entity;
import tech.metavm.object.type.ClassType;

public class ValueFoo extends Entity {

    private final ClassType classType;

    public ValueFoo(ClassType classType) {
        this.classType = classType;
    }

    public ClassType getClassType() {
        return classType;
    }
}
