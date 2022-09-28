package tech.metavm.object.instance.query;

import tech.metavm.object.meta.Field;

public class FieldExpression extends Expression {

    private final String fieldPath;
    private final Field field;

    public FieldExpression(String fieldPath, Field field) {
        this.fieldPath = fieldPath;
        this.field = field;
    }

    public Field getField() {
        return field;
    }

    public String getFieldPath() {
        return fieldPath;
    }

    @Override
    public String toString() {
        return fieldPath;
    }
}
