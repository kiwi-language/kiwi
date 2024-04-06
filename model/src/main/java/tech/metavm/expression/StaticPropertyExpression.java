package tech.metavm.expression;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.SerializeContext;
import tech.metavm.flow.Method;
import tech.metavm.object.instance.core.FlowInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Property;
import tech.metavm.object.type.Type;

import java.util.List;
import java.util.Objects;

@EntityType("静态属性表达式")
public class StaticPropertyExpression extends Expression {

    @EntityField("属性")
    private final Property property;

    public StaticPropertyExpression(@NotNull Property property) {
        this.property = property;
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        try(var serContext = SerializeContext.enter()) {
            if(serContext.isIncludeExpressionType()) {
                serContext.writeType(property.getDeclaringType());
            }
            return switch (symbolType) {
                case NAME -> property.getDeclaringType().getName() + "." + property.getName();
                case ID -> idVarName(property.getDeclaringType().getId()) + "." +
                        idVarName(property.getIdRequired());
            };
        }
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return property.getType();
    }

    @Override
    public List<Expression> getChildren() {
        return List.of();
    }

    public Property getProperty() {
        return property;
    }

    @Override
    protected Instance evaluateSelf(EvaluationContext context) {
        if(property instanceof Field field)
            return field.getStaticValue();
        else if (property instanceof Method method)
            return new FlowInstance(method, null);
        else
            throw new IllegalStateException("Unknown property type: " + property);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StaticPropertyExpression that)) return false;
        return Objects.equals(property, that.property);
    }

    @Override
    public int hashCode() {
        return Objects.hash(property);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitStaticFieldExpression(this);
    }
}
