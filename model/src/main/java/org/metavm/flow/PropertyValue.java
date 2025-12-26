package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Generated;
import org.metavm.wire.Wire;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.Expression;
import org.metavm.expression.PropertyExpression;
import org.metavm.expression.ThisExpression;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.PropertyRef;
import org.metavm.object.type.Type;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

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
