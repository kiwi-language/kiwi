package tech.metavm.expression;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Property;
import tech.metavm.object.type.Type;

import java.util.List;
import java.util.Objects;

import static tech.metavm.util.NncUtils.requireNonNull;

@EntityType("属性表达式")
public class PropertyExpression extends Expression {

    @ChildEntity("对象")
    private final Expression instance;

    @EntityField("属性")
    private final Property property;

    public PropertyExpression(@NotNull Expression instance, @NotNull Property property) {
        this.instance = addChild(instance.copy(), "instance");
        this.property = requireNonNull(property);
    }

    public Property getProperty() {
        return property;
    }

    @Override
    public Type getType() {
        return property.getType();
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(instance);
    }

    @Override
    protected Instance evaluateSelf(EvaluationContext context) {
        return ((ClassInstance) instance.evaluate(context)).getProperty(property, context.parameterizedFlowProvider());
    }

    @Override
    public String buildSelf(VarType symbolType) {
        String fieldsExpr = switch (symbolType) {
            case ID -> idVarName(property.getId());
            case NAME -> property.getName();
        };
        if((instance instanceof CursorExpression cursorExpression) && cursorExpression.getAlias() == null) {
            return fieldsExpr;
        }
        else {
            String instanceExpr = instance.build(symbolType, instance.precedence() > precedence());
            return instanceExpr + "." + fieldsExpr;
        }
    }

    @Override
    public int precedence() {
        return 0;
    }

    public Expression getInstance() {
        return instance;
    }

    @Override
    protected <T extends Expression> List<T> extractExpressionsRecursively(Class<T> klass) {
        return super.extractExpressionsRecursively(klass);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertyExpression that)) return false;
        return Objects.equals(instance, that.instance) && Objects.equals(property, that.property);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instance, property);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitPropertyExpression(this);
    }
}
