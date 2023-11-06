package tech.metavm.expression;

import tech.metavm.flow.NodeRT;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.meta.Property;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.StandardTypes;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ExpressionUtil {

    public static Expression thisObject(ClassType type) {
        return new ThisExpression(type);
    }

    public static Expression not(Expression expression) {
        if (isConstantFalse(expression)) {
            return trueExpression();
        } else if (isConstantTrue(expression)) {
            return falseExpression();
        } else if (expression instanceof UnaryExpression unaryExpression
                && unaryExpression.getOperator() == Operator.NOT) {
            return unaryExpression.getOperand();
        } else if (expression instanceof BinaryExpression binaryExpression) {
            var op = binaryExpression.getOperator();
            if (op == Operator.AND) {
                return new BinaryExpression(
                        Operator.OR,
                        not(binaryExpression.getFirst()),
                        not(binaryExpression.getSecond())
                );
            } else if (op == Operator.OR) {
                return new BinaryExpression(
                        Operator.AND,
                        not(binaryExpression.getFirst()),
                        not(binaryExpression.getSecond())
                );
            } else {
                return new BinaryExpression(
                        op.negate(),
                        binaryExpression.getFirst(),
                        binaryExpression.getSecond()
                );
            }
        } else {
            return new UnaryExpression(
                    Operator.NOT,
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
                Operator.OR,
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
                Operator.AND,
                first,
                second
        );
    }

    public static Expression isNull(Expression operand) {
        return new UnaryExpression(
                Operator.IS_NULL,
                operand
        );
    }

    public static Expression isNotNull(Expression operand) {
        return new UnaryExpression(
                Operator.IS_NOT_NULL,
                operand
        );
    }

    public static Expression add(Expression first, Expression second) {
        return new BinaryExpression(Operator.ADD, first, second);
    }

    public static Expression constant(Instance value) {
        return new ConstantExpression(value);
    }

    public static Expression constantString(String str) {
        return new ConstantExpression(InstanceUtils.stringInstance(str));
    }

    public static Expression constantLong(long value) {
        return new ConstantExpression(InstanceUtils.longInstance(value));
    }

    public static Expression fieldStartsWith(Field field, PrimitiveInstance strInstance) {
        return new FunctionExpression(
                Function.STARTS_WITH,
                List.of(
                        attributeExpr(field),
                        new ConstantExpression(strInstance)
                )
        );
    }

    public static Expression fieldLike(Field field, PrimitiveInstance strInstance) {
        return new FunctionExpression(
                Function.CONTAINS,
                List.of(
                        attributeExpr(field),
                        new ConstantExpression(strInstance)
                )
        );
    }

    public static Expression arrayAccess(Expression array, Expression index) {
        return new ArrayAccessExpression(array, index);
    }

    public static Expression nodeProp(NodeRT<?> node, Property property) {
        return prop(new NodeExpression(node), property);
    }

    public static Expression prop(Expression self, Property property) {
        return new PropertyExpression(self, property);
    }

    public static Expression fieldEq(Field field, Instance value) {
        return new BinaryExpression(
                Operator.EQ,
                attributeExpr(field),
                new ConstantExpression(value)
        );
    }

    public static PropertyExpression attributeExpr(Property attribute) {
        return new PropertyExpression(thisObject(attribute.getDeclaringType()), attribute);
    }

    public static Expression fieldIn(Field field, Collection<? extends Instance> values) {
        return new BinaryExpression(
                Operator.IN,
                attributeExpr(field),
                new ArrayExpression(
                        NncUtils.map(values, ConstantExpression::new),
                        StandardTypes.getObjectArrayType()
                )
        );
    }

    public static Expression negate(Expression expression) {
        return new NegateTransformer().transformExpression(expression);
    }

    private static class NegateTransformer extends ElementTransformer {

        @Override
        public Expression transformPropertyExpression(PropertyExpression expression) {
            return defaultNegate(expression);
        }

        @Override
        public Expression transformStaticFieldExpression(StaticFieldExpression expression) {
            return defaultNegate(expression);
        }

        @Override
        public Expression transformArrayAccessExpression(ArrayAccessExpression expression) {
            return defaultNegate(expression);
        }

        private Expression defaultNegate(Expression expression) {
            return new UnaryExpression(Operator.NOT, copyExpression(expression));
        }

        @Override
        public Expression transformBinaryExpression(BinaryExpression expression) {
            var op = expression.getOperator();
            if (op == Operator.AND) {
                return new BinaryExpression(
                        Operator.OR,
                        transformExpression(expression.getFirst()),
                        transformExpression(expression.getSecond())
                );
            }
            if (op == Operator.OR) {
                return new BinaryExpression(
                        Operator.AND,
                        transformExpression(expression.getFirst()),
                        transformExpression(expression.getSecond())
                );
            } else {
                return new BinaryExpression(
                        op.negate(),
                        copyExpression(expression.getFirst()),
                        copyExpression(expression.getSecond())
                );
            }
        }

        @Override
        public Expression transformUnaryExpression(UnaryExpression expression) {
            var op = expression.getOperator();
            if (op == Operator.NOT) {
                return copyExpression(expression.getOperand());
            } else {
                return new UnaryExpression(op.negate(), copyExpression(expression.getOperand()));
            }
        }

        @Override
        public Expression transformConstantExpression(ConstantExpression expression) {
            if (ExpressionUtil.isConstantTrue(expression)) {
                return ExpressionUtil.falseExpression();
            } else if (ExpressionUtil.isConstantFalse(expression)) {
                return ExpressionUtil.trueExpression();
            } else {
                throw new InternalException("Can not negate non-boolean constant expression: " + expression);
            }
        }
    }

    private static final ElementTransformer ELEMENT_TRANSFORMER = new ElementTransformer();

    public static Expression copyExpression(Expression expression) {
        return ELEMENT_TRANSFORMER.transformExpression(expression);
    }


    public static Expression subtract(Expression first, Expression second) {
        return new BinaryExpression(Operator.MINUS, first, second);
    }

    public static Expression multiply(Expression first, Expression second) {
        return new BinaryExpression(Operator.MULTIPLY, first, second);
    }

    public static Expression divide(Expression first, Expression second) {
        return new BinaryExpression(Operator.DIVIDE, first, second);
    }

    public static Expression mod(Expression first, Expression second) {
        return new BinaryExpression(Operator.MOD, first, second);
    }

    public static Expression eq(Expression first, Expression second) {
        return new BinaryExpression(Operator.EQ, first, second);
    }

    public static Expression ne(Expression first, Expression second) {
        return new BinaryExpression(Operator.NE, first, second);
    }

    public static Expression gt(Expression first, Expression second) {
        return new BinaryExpression(Operator.GT, first, second);
    }

    public static Expression ge(Expression first, Expression second) {
        return new BinaryExpression(Operator.GE, first, second);
    }

    public static Expression lt(Expression first, Expression second) {
        return new BinaryExpression(Operator.LT, first, second);
    }

    public static Expression le(Expression first, Expression second) {
        return new BinaryExpression(Operator.LE, first, second);
    }

    public static boolean isAllInteger(Object first, Object second) {
        return ValueUtil.isInteger(first) && ValueUtil.isInteger(second);
    }

    public static boolean isAllNumeric(Object first, Object second) {
        return ValueUtil.isNumber(first) && ValueUtil.isNumber(second);
    }

    public static Expression trueExpression() {
        return new ConstantExpression(InstanceUtils.trueInstance());
    }

    public static Expression nullExpression() {
        return new ConstantExpression(InstanceUtils.nullInstance());
    }

    public static Expression falseExpression() {
        return new ConstantExpression(InstanceUtils.falseInstance());
    }

    public static boolean isConstantTrue(Expression expression) {
        if (expression instanceof ConstantExpression constantExpression) {
            return InstanceUtils.isTrue(constantExpression.getValue());
        } else {
            return false;
        }
    }


    public static boolean isConstantFalse(Expression expression) {
        if (expression instanceof ConstantExpression constantExpression) {
            return InstanceUtils.isFalse(constantExpression.getValue());
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
            throw BusinessException.invalidExpressionValue("整数", value);
        }
    }

    public static DoubleInstance castFloat(Instance value) {
        if (value instanceof DoubleInstance doubleInstance) {
            return doubleInstance;
        }
        if (value instanceof LongInstance longInstance) {
            return InstanceUtils.doubleInstance(longInstance.getValue());
        } else {
            throw BusinessException.invalidExpressionValue("数值", value);
        }
    }

    public static BooleanInstance castBoolean(Instance value) {
        if (value instanceof BooleanInstance booleanInstance) {
            return booleanInstance;
        } else {
            throw BusinessException.invalidExpressionValue("布尔", value);
        }
    }

    public static StringInstance castString(Instance value) {
        if (value instanceof StringInstance stringInstance) {
            return stringInstance;
        } else {
            return InstanceUtils.stringInstance(Objects.toString(value.getTitle()));
        }
    }

    public static ArrayInstance castCollection(Instance value) {
        if (value instanceof ArrayInstance arrayInstance) {
            return arrayInstance;
        } else {
            throw BusinessException.invalidExpressionValue("集合", value);
        }
    }

    public static String constantToExpression(FieldValue fieldValue) {
        if (fieldValue instanceof PrimitiveFieldValue primitiveFieldValue) {
            Object value = primitiveFieldValue.getValue();
            if (value instanceof String str) {
                return "'" + str + "'";
            } else {
                return value.toString();
            }
        }
        if (fieldValue instanceof ReferenceFieldValueDTO refFieldValue) {
            return Constants.CONSTANT_ID_PREFIX + refFieldValue.getId();
        }
        if (fieldValue instanceof ArrayFieldValueDTO arrayFieldValue) {
            return "[" + NncUtils.join(arrayFieldValue.getElements(), ExpressionUtil::constantToExpression) + "]";
        }
        if (fieldValue instanceof ExpressionFieldValueDTO exprFieldValue) {
            return exprFieldValue.getExpression();
        }
        throw new InternalException("Can not convert value '" + fieldValue + "' to expression");
    }

    public static FieldValue expressionToConstant(ConstantExpression expression) {
        return expression.getValue().toFieldValueDTO();
    }

    public static @Nullable String getAlias(Expression expression) {
        return expression instanceof AsExpression asExpression ? asExpression.getAlias() : null;
    }

    public InternalException notContextExpression(Expression expression, EvaluationContext context) {
        return new InternalException(expression + " is not a context expression of " + context);
    }

    public static long parseIdFromConstantVar(String var) {
        if (var.startsWith(Constants.CONSTANT_ID_PREFIX)) {
            return Long.parseLong(var.substring(Constants.CONSTANT_ID_PREFIX.length()));
        }
        throw new InternalException("Path item '" + var + "' does not represent an identity");
    }

}
