package org.metavm.autograph;

import org.metavm.expression.BinaryExpression;
import org.metavm.expression.BinaryOperator;
import org.metavm.expression.Expression;
import org.metavm.expression.UnaryExpression;

public interface ExpressionBuilder {

    BinaryExpression createBinary(Expression first, BinaryOperator operator, Expression second);

    UnaryExpression createUnary(Expression operand, BinaryOperator operator);


}
