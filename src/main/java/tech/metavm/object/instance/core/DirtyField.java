package tech.metavm.object.instance.core;

import tech.metavm.entity.Entity;
import tech.metavm.object.type.ClassType;
import tech.metavm.util.Column;

public class DirtyField extends Entity {

    private final ClassType declaringType;
    private final Column column;
    private Instance value;

    public DirtyField(ClassType declaringType, Column column, Instance value) {
        this.declaringType = declaringType;
        this.column = column;
        this.value = value;
    }

    public ClassType getDeclaringType() {
        return declaringType;
    }

    public Column getColumn() {
        return column;
    }

    public Instance getValue() {
        return value;
    }

    public void setValue(Instance value) {
        this.value = value;
    }
}
