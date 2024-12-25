package org.metavm.expression;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.MethodRef;
import org.metavm.object.instance.core.FlowValue;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.util.ContextUtil;

import java.util.List;
import java.util.Objects;

@Entity
public class StaticPropertyExpression extends Expression {

    private final PropertyRef propertyRef;

    public StaticPropertyExpression(@NotNull PropertyRef propertyRef) {
        this.propertyRef = propertyRef;
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        try(var serContext = SerializeContext.enter()) {
            var property = getProperty();
            return switch (symbolType) {
                case NAME -> property.getDeclaringType().getName() + "." + property.getName();
                case ID -> idVarName(serContext.getId(property.getDeclaringType())) + "." +
                        idVarName(serContext.getId(property));
            };
        }
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return propertyRef.getPropertyType();
    }

    @Override
    public List<Expression> getChildren() {
        return List.of();
    }

    public Property getProperty() {
        return propertyRef.getProperty();
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        if(propertyRef instanceof FieldRef fieldRef) {
            var staticFieldTable = StaticFieldTable.getInstance(fieldRef.getDeclaringType(), ContextUtil.getEntityContext());
            return staticFieldTable.get(fieldRef.getRawField());
        }
        else if (propertyRef instanceof MethodRef method)
            return new FlowValue(method, null);
        else
            throw new IllegalStateException("Unknown property type: " + propertyRef);
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
