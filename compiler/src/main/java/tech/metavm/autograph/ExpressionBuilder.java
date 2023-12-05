package tech.metavm.autograph;

import tech.metavm.expression.BinaryExpression;
import tech.metavm.expression.BinaryOperator;
import tech.metavm.expression.Expression;
import tech.metavm.expression.UnaryExpression;

public interface ExpressionBuilder {

    BinaryExpression createBinary(Expression first, BinaryOperator operator, Expression second);

    UnaryExpression createUnary(Expression operand, BinaryOperator operator);


}
