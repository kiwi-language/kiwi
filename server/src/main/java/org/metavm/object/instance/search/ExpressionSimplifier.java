package org.metavm.object.instance.search;

import org.metavm.entity.CopyVisitor;
import org.metavm.entity.Element;
import org.metavm.expression.ConstantExpression;
import org.metavm.expression.Expression;
import org.metavm.expression.UnaryExpression;
import org.metavm.expression.UnaryOperator;
import org.metavm.object.instance.core.BooleanValue;
import org.metavm.object.instance.core.NumberValue;

public class ExpressionSimplifier extends CopyVisitor {

    public static Expression simplify(Expression expression) {
        return (Expression) expression.accept(new ExpressionSimplifier(expression));
    }

    public ExpressionSimplifier(Expression root) {
        super(root, false);
    }

    @Override
    public Element visitUnaryExpression(UnaryExpression expression) {
        var operand = (Expression) copy(expression.getOperand());
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
