package org.manul.flow;

import org.jetbrains.annotations.NotNull;
import org.manul.api.Generated;
import org.manul.wire.Wire;
import org.manul.expression.EvaluationContext;
import org.manul.expression.Expression;
import org.manul.object.instance.core.Reference;
import org.manul.object.type.Type;
import org.manul.util.MvInput;
import org.manul.util.MvOutput;
import org.manul.util.StreamVisitor;

import java.util.function.Consumer;

@Wire
public class NeverValue extends Value {

    @Generated
    public static NeverValue read(MvInput input) {
        return new NeverValue();
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
    }

    public Type getType() {
        return null;
    }

    @Override
    public org.manul.object.instance.core.@NotNull Value evaluate(EvaluationContext context) {
        throw new IllegalStateException("NeverValue should not be evaluated");
    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public Expression getExpression() {
        return null;
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_NeverValue);
        super.write(output);
    }
}
