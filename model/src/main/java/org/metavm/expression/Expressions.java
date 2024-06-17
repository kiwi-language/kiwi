package org.metavm.expression;

import org.metavm.flow.NodeRT;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.rest.*;
import org.metavm.object.type.*;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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

    public static Expression constant(Instance value) {
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

    public static Expression fieldStartsWith(Field field, PrimitiveInstance strInstance) {
        return new FunctionExpression(
                Func.STARTS_WITH,
                List.of(
                        propertyExpr(field),
                        new ConstantExpression(strInstance)
                )
        );
    }

    public static Expression fieldLike(Field field, PrimitiveInstance strInstance) {
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

    public static Expression node(NodeRT node) {
        return new NodeExpression(node);
    }

    public static Expression nodeProperty(NodeRT node, Property property) {
        return property(new NodeExpression(node), property);
    }

    public static Expression arrayLength(Expression array) {
        return new FunctionExpression(Func.LEN, array);
    }

    public static Expression property(Expression self, Property property) {
        return new PropertyExpression(self, property.getRef());
    }

    public static Expression fieldEq(Field field, Instance value) {
        return new BinaryExpression(
                BinaryOperator.EQ,
                propertyExpr(field),
                new ConstantExpression(value)
        );
    }

    public static PropertyExpression propertyExpr(Property property) {
        return new PropertyExpression(thisObject(property.getDeclaringType()), property.getRef());
    }

    public static Expression fieldIn(Field field, Collection<? extends Instance> values) {
        return new BinaryExpression(
                BinaryOperator.IN,
                propertyExpr(field),
                new ArrayExpression(
                        NncUtils.map(values, ConstantExpression::new),
                        Types.getAnyArrayType()
                )
        );
    }

    public static Expression subtract(Expression first, Expression second) {
        return new BinaryExpression(BinaryOperator.MINUS, first, second);
    }

    public static Expression multiply(Expression first, Expression second) {
        return new BinaryExpression(BinaryOperator.MULTIPLY, first, second);
    }

    public static Expression divide(Expression first, Expression second) {
        return new BinaryExpression(BinaryOperator.DIVIDE, first, second);
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

    public static LongInstance castInteger(Instance value) {
        if (value instanceof LongInstance longInstance) {
            return longInstance;
        } else {
            throw BusinessException.invalidExpressionValue("integer", value);
        }
    }

    public static DoubleInstance castFloat(Instance value) {
        if (value instanceof DoubleInstance doubleInstance) {
            return doubleInstance;
        }
        if (value instanceof LongInstance longInstance) {
            return Instances.doubleInstance(longInstance.getValue());
        } else {
            throw BusinessException.invalidExpressionValue("float", value);
        }
    }

    public static BooleanInstance castBoolean(Instance value) {
        if (value instanceof BooleanInstance booleanInstance) {
            return booleanInstance;
        } else {
            throw BusinessException.invalidExpressionValue("boolean", value);
        }
    }

    public static StringInstance castString(Instance value) {
        if (value instanceof StringInstance stringInstance) {
            return stringInstance;
        } else {
            return Instances.stringInstance(Objects.toString(value.getTitle()));
        }
    }

    public static ArrayInstance castCollection(Instance value) {
        if (value instanceof ArrayInstance arrayInstance) {
            return arrayInstance;
        } else {
            throw BusinessException.invalidExpressionValue("Collection", value);
        }
    }

    public static String constantToExpression(FieldValue fieldValue) {
        if (fieldValue instanceof PrimitiveFieldValue primitiveFieldValue) {
            Object value = primitiveFieldValue.getValue();
            if(primitiveFieldValue.getPrimitiveKind() == PrimitiveKind.NULL.code())
                return "null";
            else if (value instanceof String str) {
                return "'" + str + "'";
            } else if (primitiveFieldValue.getPrimitiveKind() == PrimitiveKind.TIME.code()) {
                return String.format("TIME(%d)", (long) primitiveFieldValue.getValue());
            } else {
                return value.toString();
            }
        }
        if (fieldValue instanceof ReferenceFieldValue refFieldValue) {
            return Constants.ID_PREFIX + refFieldValue.getId();
        }
        if (fieldValue instanceof ArrayFieldValue arrayFieldValue) {
            return "[" + NncUtils.join(arrayFieldValue.getElements(), Expressions::constantToExpression) + "]";
        }
        if (fieldValue instanceof ExpressionFieldValue exprFieldValue) {
            return exprFieldValue.getExpression();
        }
        throw new InternalException("Can not convert value '" + fieldValue + "' to expression");
    }

    public static @Nullable String getAlias(Expression expression) {
        return expression instanceof AsExpression asExpression ? asExpression.getAlias() : null;
    }

    public static Expression constantBoolean(boolean bool) {
        return new ConstantExpression(Instances.booleanInstance(bool));
    }

    public InternalException notContextExpression(Expression expression, EvaluationContext context) {
        return new InternalException(expression + " is not a context expression of " + context);
    }

    public static String parseIdFromConstantVar(String var) {
        if (var.startsWith(Constants.ID_PREFIX)) {
            return var.substring(Constants.ID_PREFIX.length());
        }
        throw new InternalException("Path item '" + var + "' does not represent an identity");
    }

    public static String deEscapeDoubleQuoted(String escaped) {
        NncUtils.requireTrue(escaped.length() >= 2);
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
        NncUtils.requireTrue(escaped.length() >= 2);
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

}
