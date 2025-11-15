package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.wire.Wire;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.Expression;
import org.metavm.expression.VarType;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Type;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.function.Consumer;

@Wire
@Entity
public class ExpressionValue extends Value {

    private final Expression expression;

    public ExpressionValue(@NotNull Expression expression) {
        this.expression = expression;
    }

    @Generated
    public static ExpressionValue read(MvInput input) {
        return new ExpressionValue(Expression.read(input));
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        Expression.visit(visitor);
    }

    @Override
    public Type getType() {
        return expression.getType();
    }

    @Override
    public org.metavm.object.instance.core.@NotNull Value evaluate(EvaluationContext context) {
        return expression.evaluate(context);
    }

    @Override
    public String getText() {
        return expression.build(VarType.NAME);
    }

    @Override
    public Expression getExpression() {
        return expression;
    }


    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        expression.forEachReference(action);
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_ExpressionValue);
        super.write(output);
        expression.write(output);
    }
}
