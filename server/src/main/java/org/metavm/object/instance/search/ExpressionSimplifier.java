package org.metavm.object.instance.search;

import org.jetbrains.annotations.NotNull;
import org.metavm.expression.*;
import org.metavm.object.instance.core.BooleanValue;
import org.metavm.object.instance.core.NumberValue;

public class ExpressionSimplifier extends ExpressionTransformer {

    public static Expression simplify(@NotNull Expression expression) {
        return expression.accept(new ExpressionSimplifier());
    }

    @Override
    public Expression visitUnaryExpression(UnaryExpression expression) {
        var operand = expression.getOperand().copy();
        var operator = expression.getOperator();
        if(operator == UnaryOperator.NOT) {
            if(operand instanceof ConstantExpression constExpr)
                return new ConstantExpression(((BooleanValue) constExpr.getValue()).not());
            else if(operand instanceof UnaryExpression unaryExpr && unaryExpr.getOperator() == UnaryOperator.NOT)
                return unaryExpr.getOperand();
        }
        else if(operator == UnaryOperator.NEG) {
            if(operand instanceof ConstantExpression constExpr)
                return new ConstantExpression(((NumberValue) constExpr.getValue()).negate());
        }
        return new UnaryExpression(operator, operand);
    }
}
