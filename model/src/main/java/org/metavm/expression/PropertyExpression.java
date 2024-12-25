package org.metavm.expression;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Property;
import org.metavm.object.type.PropertyRef;
import org.metavm.object.type.Type;

import java.util.List;
import java.util.Objects;

import static org.metavm.util.NncUtils.requireNonNull;

@Entity
public class PropertyExpression extends Expression {

    private final Expression instance;

    private final PropertyRef propertyRef;

    public PropertyExpression(@NotNull Expression instance, @NotNull PropertyRef propertyRef) {
        this.instance = instance;
        this.propertyRef = propertyRef;
    }

    public Property getProperty() {
        return propertyRef.getProperty();
    }

    @Override
    public Type getType() {
        return propertyRef.getPropertyType();
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(instance);
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        return (instance.evaluate(context)).resolveObject().getProperty(propertyRef);
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        String fieldsExpr = switch (symbolType) {
            case ID -> {
                try (var serContext = SerializeContext.enter()) {
                    yield idVarName(requireNonNull(serContext.getId(getProperty())));
                }
            }
            case NAME -> getProperty().getName();
        };
        if((instance instanceof CursorExpression cursorExpression) && cursorExpression.getAlias() == null) {
            return fieldsExpr;
        }
        else {
            String instanceExpr = instance.build(symbolType, instance.precedence() > precedence(), relaxedCheck);
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
        return Objects.equals(instance, that.instance) && Objects.equals(propertyRef, that.propertyRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instance, propertyRef);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitPropertyExpression(this);
    }
}
