package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Generated;
import org.metavm.wire.Wire;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.Expression;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Type;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

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
    public org.metavm.object.instance.core.@NotNull Value evaluate(EvaluationContext context) {
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
