package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.Expression;
import org.metavm.expression.PropertyExpression;
import org.metavm.expression.ThisExpression;
import org.metavm.object.type.PropertyRef;
import org.metavm.object.type.Type;

import java.util.Objects;

public class PropertyValue extends Value {

    private final PropertyRef propertyRef;

    public PropertyValue(PropertyRef propertyRef) {
        this.propertyRef = propertyRef;
    }

    @Override
    public Type getType() {
        return Objects.requireNonNull(propertyRef.getType());
    }

    @Override
    public org.metavm.object.instance.core.@NotNull Value evaluate(EvaluationContext context) {
        return getExpression().evaluate(context);
    }

    @Override
    public String getText() {
        return propertyRef.getName();
    }

    @Override
    public Expression getExpression() {
        return new PropertyExpression(
                new ThisExpression(propertyRef.getDeclaringType()),
                propertyRef
        );
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitPropertyValue(this);
    }
}
