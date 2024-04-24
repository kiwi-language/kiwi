package tech.metavm.object.instance.core;

import tech.metavm.entity.Entity;
import tech.metavm.object.type.Klass;
import tech.metavm.util.Column;

public class UnknownField extends Entity {

    private final Klass declaringType;
    private final Column column;
    private Instance value;

    public UnknownField(Klass declaringType, Column column, Instance value) {
        this.declaringType = declaringType;
        this.column = column;
        this.value = value;
    }

    public Klass getDeclaringType() {
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
