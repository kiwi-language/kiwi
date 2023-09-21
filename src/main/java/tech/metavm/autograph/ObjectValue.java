package tech.metavm.autograph;

import tech.metavm.expression.Expression;
import tech.metavm.expression.FieldExpression;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;

import java.util.HashMap;
import java.util.Map;

public class ObjectValue {

    private final Expression instance;
    private final Map<String, Expression> fieldValues = new HashMap<>();

    public ObjectValue(Expression instance) {
        this.instance = instance;
    }

    public Expression getField(String fieldCode) {
        return fieldValues.computeIfAbsent(fieldCode, this::createFieldValue);
    }

    private Expression createFieldValue(String fieldCode) {
        Field field = ((ClassType)instance.getType()).getFieldByCode(fieldCode);
        return new FieldExpression(instance, field);
    }

    public void setField(String fieldCode, Expression value) {
        fieldValues.put(fieldCode, value);
    }

}
