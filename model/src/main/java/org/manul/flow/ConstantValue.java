package org.manul.flow;

import org.jetbrains.annotations.NotNull;
import org.manul.api.Entity;
import org.manul.api.Generated;
import org.manul.wire.Wire;
import org.manul.expression.ConstantExpression;
import org.manul.expression.EvaluationContext;
import org.manul.expression.Expression;
import org.manul.object.instance.core.Reference;
import org.manul.object.type.Type;
import org.manul.util.MvInput;
import org.manul.util.MvOutput;
import org.manul.util.StreamVisitor;

import java.util.function.Consumer;

@Wire
@Entity
public class ConstantValue extends Value {

    private final org.manul.object.instance.core.Value value;

    public ConstantValue(org.manul.object.instance.core.Value value) {
        this.value = value;
    }

    @Generated
    public static ConstantValue read(MvInput input) {
        return new ConstantValue(input.readValue());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitValue();
    }


    @Override
    public Type getType() {
        return value.getValueType();
    }

    @Override
    public org.manul.object.instance.core.@NotNull Value evaluate(EvaluationContext context) {
        return value;
    }

    @Override
    public String getText() {
        return value.getText();
    }

    @Override
    public Expression getExpression() {
        return new ConstantExpression(value);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        if (value instanceof Reference r) action.accept(r);
        else if (value instanceof org.manul.object.instance.core.NativeValue t) t.forEachReference(action);
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_ConstantValue);
        super.write(output);
        output.writeValue(value);
    }
}
