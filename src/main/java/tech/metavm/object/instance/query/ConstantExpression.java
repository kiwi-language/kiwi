package tech.metavm.object.instance.query;

import java.util.Objects;

public class ConstantExpression extends Expression {
    private final Object value;

    public ConstantExpression(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        if(value instanceof String) {
            return "'" + ((String) value).replaceAll("'", "''") + "'";
        }
        return Objects.toString(value);
    }
}
