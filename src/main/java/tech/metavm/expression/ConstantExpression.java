package tech.metavm.expression;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.PrimitiveInstance;
import tech.metavm.object.instance.StringInstance;
import tech.metavm.object.meta.Type;
import tech.metavm.util.Constants;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ValueUtil;

import java.util.List;
import java.util.Objects;

@EntityType("常量表达式")
public class ConstantExpression extends Expression {

    @EntityField(value = "常量值", asTitle = true)
    private final Instance value;

    public ConstantExpression(Instance value) {
        this.value = value;
    }

    public Instance getValue() {
        return value;
    }

    @Override
    public String buildSelf(VarType symbolType) {
        if(value instanceof StringInstance stringInstance) {
            return "'" + stringInstance.getValue().replaceAll("'", "''") + "'";
        }
        else if(value instanceof PrimitiveInstance primitiveInstance) {
            return primitiveInstance.getValue() + "";
        }
        else {
            return Constants.CONSTANT_ID_PREFIX + NncUtils.requireNonNull(value.getId());
        }
    }

    @Override
    public Type getType() {
        return value.getType();
    }

    @Override
    protected List<Expression> getChildren() {
        return List.of();
    }

    @Override
    public Expression cloneWithNewChildren(List<Expression> children) {
        return new ConstantExpression(value);
    }

    public boolean isString() {
        return value instanceof StringInstance;
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConstantExpression that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
