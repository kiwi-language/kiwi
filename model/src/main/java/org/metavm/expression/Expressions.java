package org.metavm.expression;

import org.metavm.flow.Node;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Property;
import org.metavm.util.Instances;
import org.metavm.util.Utils;
import org.metavm.util.ValueUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Expressions {

    public static Expression thisObject(Klass klass) {
        return new ThisExpression(klass.getType());
    }

    public static Expression not(Expression expression) {
        if (isConstantFalse(expression)) {
            return trueExpression();
        } else if (isConstantTrue(expression)) {
            return falseExpression();
        } else if (expression instanceof UnaryExpression unaryExpression
                && unaryExpression.getOperator() == UnaryOperator.NOT) {
            return unaryExpression.getOperand();
        } else if (expression instanceof BinaryExpression binaryExpression) {
            var op = binaryExpression.getOperator();
            if (op == BinaryOperator.AND) {
                return new BinaryExpression(
                        BinaryOperator.OR,
                        not(binaryExpression.getLeft()),
                        not(binaryExpression.getRight())
                );
            } else if (op == BinaryOperator.OR) {
                return new BinaryExpression(
                        BinaryOperator.AND,
                        not(binaryExpression.getLeft()),
                        not(binaryExpression.getRight())
                );
            } else {
                return new BinaryExpression(
                        op.complement(),
                        binaryExpression.getLeft(),
                        binaryExpression.getRight()
                );
            }
        } else {
            return new UnaryExpression(
                    UnaryOperator.NOT,
                    expression
            );
        }
    }

    public static Expression or(Expression first, Expression second) {
        if (isConstantTrue(first) || isConstantTrue(second)) {
            return trueExpression();
        }
        if (isConstantFalse(first)) {
            return second;
        }
        if (isConstantFalse(second)) {
            return first;
        }
        return new BinaryExpression(
                BinaryOperator.OR,
                first,
                second
        );
    }

    public static Expression and(Expression first, Expression second) {
        if (isConstantFalse(first) || isConstantFalse(second)) {
            return falseExpression();
        }
        if (isConstantTrue(first)) {
            return second;
        }
        if (isConstantTrue(second)) {
            return first;
        }
        return new BinaryExpression(
                BinaryOperator.AND,
                first,
                second
        );
    }

    public static Expression isNull(Expression operand) {
        return new UnaryExpression(
                UnaryOperator.IS_NULL,
                operand
        );
    }

    public static Expression isNotNull(Expression operand) {
        return new UnaryExpression(
                UnaryOperator.IS_NOT_NULL,
                operand
        );
    }

    public static Expression add(Expression first, Expression second) {
        return new BinaryExpression(BinaryOperator.ADD, first, second);
    }

    public static Expression constant(Value value) {
        return new ConstantExpression(value);
    }

    public static Expression constantString(String str) {
        return new ConstantExpression(Instances.stringInstance(str));
    }

    public static Expression constantLong(long value) {
        return new ConstantExpression(Instances.longInstance(value));
    }

    public static Expression constantDouble(double value) {
        return new ConstantExpression(Instances.doubleInstance(value));
    }

    public static Expression constantChar(char c) {
        return new ConstantExpression(Instances.charInstance(c));
    }

    public static Expression fieldStartsWith(Field field, Value strInstance) {
        return new FunctionExpression(
                Func.STARTS_WITH,
                List.of(
                        propertyExpr(field),
                        new ConstantExpression(strInstance)
                )
        );
    }

    public static Expression fieldLike(Field field, Value strInstance) {
        return new FunctionExpression(
                Func.CONTAINS,
                List.of(
                        propertyExpr(field),
                        new ConstantExpression(strInstance)
                )
        );
    }

    public static Expression arrayAccess(Expression array, Expression index) {
        return new ArrayAccessExpression(array, index);
    }

    public static Expression node(Node node) {
        return new NodeExpression(node);
    }

    public static Expression nodeProperty(Node node, Property property) {
        return property(new NodeExpression(node), property);
    }

    public static Expression property(Expression self, Property property) {
        return new PropertyExpression(self, property.getRef());
    }

    public static Expression fieldEq(Field field, Value value) {
        return new BinaryExpression(
                BinaryOperator.EQ,
                propertyExpr(field),
                new ConstantExpression(value)
        );
    }

    public static PropertyExpression propertyExpr(Property property) {
        return new PropertyExpression(thisObject(property.getDeclaringType()), property.getRef());
    }

    public static Expression fieldIn(Field field, Collection<? extends Value> values) {
        return new BinaryExpression(
                BinaryOperator.IN,
                propertyExpr(field),
                new ConstantExpression(Instances.arrayValue(new ArrayList<>(values)))
        );
    }

    public static Expression sub(Expression first, Expression second) {
        return new BinaryExpression(BinaryOperator.SUB, first, second);
    }

    public static Expression mul(Expression first, Expression second) {
        return new BinaryExpression(BinaryOperator.MUL, first, second);
    }

    public static Expression div(Expression first, Expression second) {
        return new BinaryExpression(BinaryOperator.DIV, first, second);
    }

    public static Expression mod(Expression first, Expression second) {
        return new BinaryExpression(BinaryOperator.MOD, first, second);
    }

    public static Expression eq(Expression first, Expression second) {
        return new BinaryExpression(BinaryOperator.EQ, first, second);
    }

    public static Expression ne(Expression first, Expression second) {
        return new BinaryExpression(BinaryOperator.NE, first, second);
    }

    public static Expression gt(Expression first, Expression second) {
        return new BinaryExpression(BinaryOperator.GT, first, second);
    }

    public static Expression ge(Expression first, Expression second) {
        return new BinaryExpression(BinaryOperator.GE, first, second);
    }

    public static Expression lt(Expression first, Expression second) {
        return new BinaryExpression(BinaryOperator.LT, first, second);
    }

    public static Expression func(Func func, Expression...arguments) {
        return new FunctionExpression(func, List.of(arguments));
    }

    public static Expression le(Expression first, Expression second) {
        return new BinaryExpression(BinaryOperator.LE, first, second);
    }

    public static boolean isAllInteger(Object first, Object second) {
        return ValueUtils.isInteger(first) && ValueUtils.isInteger(second);
    }

    public static boolean isAllNumeric(Object first, Object second) {
        return ValueUtils.isNumber(first) && ValueUtils.isNumber(second);
    }

    public static Expression trueExpression() {
        return new ConstantExpression(Instances.trueInstance());
    }

    public static Expression nullExpression() {
        return new ConstantExpression(Instances.nullInstance());
    }

    public static Expression falseExpression() {
        return new ConstantExpression(Instances.falseInstance());
    }

    public static boolean isConstantTrue(Expression expression) {
        if (expression instanceof ConstantExpression constantExpression) {
            return Instances.isTrue(constantExpression.getValue());
        } else {
            return false;
        }
    }

    public static boolean isConstantFalse(Expression expression) {
        if (expression instanceof ConstantExpression constantExpression) {
            return Instances.isFalse(constantExpression.getValue());
        } else {
            return false;
        }
    }

    public static boolean isConstant(Expression expression) {
        return expression instanceof ConstantExpression;
    }

    public static boolean isNotConstant(Expression expression) {
        return !isConstant(expression);
    }

    public static boolean isConstantNull(Expression expression) {
        if (expression instanceof ConstantExpression constantExpression) {
            return constantExpression.getValue() == null;
        } else {
            return false;
        }
    }

    public static Expression never() {
        return new NeverExpression();
    }

    public static String deEscapeDoubleQuoted(String escaped) {
        Utils.require(escaped.length() >= 2);
        StringBuilder builder = new StringBuilder();
        boolean lastBackslash = false;
        for (int i = 1; i < escaped.length() - 1; i++) {
            char c = escaped.charAt(i);
            if (lastBackslash) {
                if (c == '\\' || c == '\"') {
                    builder.append(c);
                    lastBackslash = false;
                } else {
                    throw new ExpressionParsingException("Invalid double escaped string '" + escaped + "'");
                }
            } else {
                if (c == '\\') {
                    lastBackslash = true;
                } else {
                    builder.append(c);
                }
            }
        }
        return builder.toString();
    }


    public static String deEscapeSingleQuoted(String escaped) {
        Utils.require(escaped.length() >= 2);
        StringBuilder builder = new StringBuilder();
        boolean lastBackslash = false;
        for (int i = 1; i < escaped.length() - 1; i++) {
            char c = escaped.charAt(i);
            if (lastBackslash) {
                if (c == '\\' || c == '\'') {
                    builder.append(c);
                    lastBackslash = false;
                } else {
                    throw new ExpressionParsingException("Invalid single escaped string '" + escaped + "'");
                }
            } else {
                if (c == '\\') {
                    lastBackslash = true;
                } else {
                    builder.append(c);
                }
            }
        }
        return builder.toString();
    }

    public static char deEscapeChar(String escaped) {
        if(escaped.length() == 3)
            return escaped.charAt(1);
        else if(escaped.length() == 4)
            return escaped.charAt(2);
        else
            throw new IllegalArgumentException("Invalid char literal: " + escaped);
    }
}
