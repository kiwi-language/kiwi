package org.metavm.expression;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.FlowInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.Field;
import org.metavm.object.type.Property;
import org.metavm.object.type.PropertyRef;
import org.metavm.object.type.Type;

import java.util.List;
import java.util.Objects;

@EntityType
public class StaticPropertyExpression extends Expression {

    private final PropertyRef propertyRef;

    public StaticPropertyExpression(@NotNull PropertyRef propertyRef) {
        this.propertyRef = propertyRef;
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
                        idVarName(property.getIdNotNull());
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
