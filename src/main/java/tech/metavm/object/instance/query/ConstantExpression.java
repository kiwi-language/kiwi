package tech.metavm.object.instance.query;

import tech.metavm.object.meta.Type;
import tech.metavm.util.ValueUtil;

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
    public String buildSelf(VarType symbolType) {
        if(value instanceof String) {
            return "'" + ((String) value).replaceAll("'", "''") + "'";
        }
        return Objects.toString(value);
    }

    @Override
    public Type getType() {
        return ValueUtil.getValueType(value);
    }

    @Override
    public int precedence() {
        return 0;
    }

}
