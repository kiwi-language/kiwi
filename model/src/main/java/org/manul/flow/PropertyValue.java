package org.manul.flow;

import org.jetbrains.annotations.NotNull;
import org.manul.api.Generated;
import org.manul.wire.Wire;
import org.manul.expression.EvaluationContext;
import org.manul.expression.Expression;
import org.manul.expression.PropertyExpression;
import org.manul.expression.ThisExpression;
import org.manul.object.instance.core.Reference;
import org.manul.object.type.PropertyRef;
import org.manul.object.type.Type;
import org.manul.util.MvInput;
import org.manul.util.MvOutput;
import org.manul.util.StreamVisitor;

import java.util.Objects;
import java.util.function.Consumer;

@Wire
public class PropertyValue extends Value {

    private final PropertyRef propertyRef;

    public PropertyValue(PropertyRef propertyRef) {
        this.propertyRef = propertyRef;
    }

    @Generated
    public static PropertyValue read(MvInput input) {
        return new PropertyValue((PropertyRef) input.readValue());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitValue();
    }

    @Override
    public Type getType() {
        return Objects.requireNonNull(propertyRef.getPropertyType());
    }

    @Override
    public org.manul.object.instance.core.@NotNull Value evaluate(EvaluationContext context) {
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

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        propertyRef.forEachReference(action);
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_PropertyValue);
        super.write(output);
        output.writeValue(propertyRef);
    }
}
