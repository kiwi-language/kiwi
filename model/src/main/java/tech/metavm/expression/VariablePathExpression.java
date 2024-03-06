package tech.metavm.expression;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.StandardTypes;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Type;

import java.util.List;
import java.util.Objects;

@EntityType("变量路径表达式")
public class VariablePathExpression extends Expression {

    @EntityField("前缀")
    private final Expression qualifier;
    private final VariableExpression field;

    public VariablePathExpression(@NotNull Expression qualifier, @NotNull VariableExpression field) {
        this.qualifier = addChild(qualifier.copy(), "qualifier");
        this.field = field;
    }

    public Expression getQualifier() {
        return qualifier;
    }

    public VariableExpression getField() {
        return field;
    }

    @Override
    public String buildSelf(VarType symbolType) {
        return qualifier.build(symbolType, false) + "." + field.buildSelf(symbolType);
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return StandardTypes.getAnyType();
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(qualifier, field);
    }

    @Override
    protected Instance evaluateSelf(EvaluationContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VariablePathExpression that)) return false;
        return Objects.equals(qualifier, that.qualifier) && Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qualifier, field);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitVariablePathExpression(this);
    }
}
