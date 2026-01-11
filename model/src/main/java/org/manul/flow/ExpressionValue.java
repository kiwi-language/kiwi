package org.manul.flow;

import org.jetbrains.annotations.NotNull;
import org.manul.api.Entity;
import org.manul.api.Generated;
import org.manul.wire.Wire;
import org.manul.expression.EvaluationContext;
import org.manul.expression.Expression;
import org.manul.expression.VarType;
import org.manul.object.instance.core.Reference;
import org.manul.object.type.Type;
import org.manul.util.MvInput;
import org.manul.util.MvOutput;
import org.manul.util.StreamVisitor;

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
    public org.manul.object.instance.core.@NotNull Value evaluate(EvaluationContext context) {
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
