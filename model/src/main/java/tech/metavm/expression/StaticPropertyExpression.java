package tech.metavm.expression;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.flow.Method;
import tech.metavm.object.instance.core.FlowInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Property;
import tech.metavm.object.type.PropertyRef;
import tech.metavm.object.type.Type;

import java.util.List;
import java.util.Objects;

@EntityType("静态属性表达式")
public class StaticPropertyExpression extends Expression {

    @ChildEntity("属性")
    private final PropertyRef propertyRef;

    public StaticPropertyExpression(@NotNull PropertyRef propertyRef) {
        this.propertyRef = addChild((Entity & PropertyRef) propertyRef.copy(), "propertyRef");
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        try(var serContext = SerializeContext.enter()) {
            var property = getProperty();
            if(serContext.isIncludeExpressionType()) {
                serContext.writeTypeDef(property.getDeclaringType());
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
        return getProperty().getType();
    }

    @Override
    public List<Expression> getChildren() {
        return List.of();
    }

    public Property getProperty() {
        return propertyRef.resolve();
    }

    @Override
    protected Instance evaluateSelf(EvaluationContext context) {
        var property = getProperty();
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
        return Objects.equals(propertyRef, that.propertyRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(propertyRef);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitStaticFieldExpression(this);
    }
}
