package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.expression.*;
import org.metavm.flow.rest.ValueDTO;
import org.metavm.object.type.PropertyRef;
import org.metavm.object.type.Type;

import java.util.Objects;

public class PropertyValue extends Value {

    private final PropertyRef propertyRef;

    public PropertyValue(PropertyRef propertyRef) {
        this.propertyRef = propertyRef;
    }

    @Override
    public ValueDTO toDTO() {
        return null;
    }

    @Override
    public Type getType() {
        return Objects.requireNonNull(propertyRef.resolve().getType());
    }

    @Override
    public org.metavm.object.instance.core.@NotNull Value evaluate(EvaluationContext context) {
        return getExpression().evaluate(context);
    }

    @Override
    public String getText() {
        return propertyRef.resolve().getName();
    }

    @Override
    public Expression getExpression() {
        return new PropertyExpression(
                new ThisExpression(propertyRef.resolve().getDeclaringType().getType()),
                propertyRef
        );
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitPropertyValue(this);
    }
}
