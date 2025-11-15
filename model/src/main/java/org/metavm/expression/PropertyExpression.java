package org.metavm.expression;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.wire.Wire;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Property;
import org.metavm.object.type.PropertyRef;
import org.metavm.object.type.Type;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Wire
@Entity
public class PropertyExpression extends Expression {

    @Getter
    private final Expression instance;

    private final PropertyRef propertyRef;

    public PropertyExpression(@NotNull Expression instance, @NotNull PropertyRef propertyRef) {
        this.instance = instance;
        this.propertyRef = propertyRef;
    }

    @Generated
    public static PropertyExpression read(MvInput input) {
        return new PropertyExpression(Expression.read(input), (PropertyRef) input.readValue());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        Expression.visit(visitor);
        visitor.visitValue();
    }

    public Property getProperty() {
        return propertyRef.getProperty();
    }

    @Override
    public Type getType() {
        return propertyRef.getPropertyType();
    }

    @Override
    public List<Expression> getComponents() {
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
                    yield idVarName(Objects.requireNonNull(serContext.getId(getProperty())));
                }
            }
            case NAME -> getProperty().getName();
        };
        String instanceExpr = instance.build(symbolType, instance.precedence() > precedence(), relaxedCheck);
        return instanceExpr + "." + fieldsExpr;
    }

    @Override
    public int precedence() {
        return 0;
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

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        instance.accept(visitor);
        propertyRef.accept(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        instance.forEachReference(action);
        propertyRef.forEachReference(action);
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_PropertyExpression);
        super.write(output);
        instance.write(output);
        output.writeValue(propertyRef);
    }

    @Override
    public Expression transform(ExpressionTransformer transformer) {
        return new PropertyExpression(instance.accept(transformer), propertyRef);
    }
}
