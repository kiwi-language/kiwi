package tech.metavm.expression;

import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.PrimitiveInstance;
import tech.metavm.object.instance.core.StringInstance;
import tech.metavm.object.type.Type;
import tech.metavm.util.Constants;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Objects;

@EntityType("常量表达式")
public class ConstantExpression extends Expression {

    @EntityField("常量值")
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
            return Constants.CONSTANT_ID_PREFIX + NncUtils.requireNonNull(value.getInstanceIdString());
        }
    }

    @Override
    public Type getType() {
        return value.getType();
    }

    @Override
    public List<Expression> getChildren() {
        return List.of();
    }

    @Override
    public Instance evaluate(EvaluationContext context) {
        return value;
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

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitConstantExpression(this);
    }

}
