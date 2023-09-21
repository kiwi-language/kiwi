package tech.metavm.expression;

import tech.metavm.object.instance.*;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;

public class ExpressionUtil {

    public static Expression thisObject(ClassType type) {
        return new ThisExpression(type);
    }

    public static Expression not(Expression expression) {
        return new UnaryExpression(
                Operator.NOT,
                expression
        );
    }

    public static Expression or(Expression first, Expression second) {
        return new BinaryExpression(
                Operator.OR,
                first,
                second
        );
    }

    public static Expression and(Expression first, Expression second) {
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
        return new BinaryExpression(
                Operator.STARTS_WITH,
                fieldExpr(field),
                new ConstantExpression(strInstance)
        );
    }

    public static Expression fieldLike(Field field, PrimitiveInstance strInstance) {
        return new BinaryExpression(
                Operator.LIKE,
                fieldExpr(field),
                new ConstantExpression(strInstance)
        );
    }

    public static Expression fieldEq(Field field, Instance value) {
        return new BinaryExpression(
                Operator.EQ,
                fieldExpr(field),
                new ConstantExpression(value)
        );
    }

    public static FieldExpression fieldExpr(Field field) {
        return new FieldExpression(thisObject(field.getDeclaringType()), field);
    }

    public static Expression fieldIn(Field field, Collection<? extends Instance> values) {
        return new BinaryExpression(
                Operator.IN,
                fieldExpr(field),
                new ArrayExpression(
                        NncUtils.map(values, ConstantExpression::new)
                )
        );
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
        if(expression instanceof ConstantExpression constantExpression) {
            return InstanceUtils.isTrue(constantExpression.getValue());
        }
        else {
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
        if(expression instanceof ConstantExpression constantExpression) {
            return constantExpression.getValue() == null;
        }
        else {
            return false;
        }
    }


    public static LongInstance castInteger(Instance value) {
        if(value instanceof LongInstance longInstance) {
            return longInstance;
        }
        else {
            throw BusinessException.invalidExpressionValue("整数", value);
        }
    }

    public static DoubleInstance castFloat(Instance value) {
        if(value instanceof DoubleInstance doubleInstance) {
            return doubleInstance;
        }
        if(value instanceof LongInstance longInstance) {
            return InstanceUtils.doubleInstance(longInstance.getValue());
        }
        else {
            throw BusinessException.invalidExpressionValue("数值", value);
        }
    }

    public static BooleanInstance castBoolean(Instance value) {
        if(value instanceof BooleanInstance booleanInstance) {
            return booleanInstance;
        }
        else {
            throw BusinessException.invalidExpressionValue("布尔", value);
        }
    }

    public static StringInstance castString(Instance value) {
        if(value instanceof StringInstance stringInstance) {
            return stringInstance;
        }
        else {
            return InstanceUtils.stringInstance(Objects.toString(value.getTitle()));
        }
    }

    public static ArrayInstance castCollection(Instance value) {
        if(value instanceof ArrayInstance arrayInstance) {
            return arrayInstance;
        }
        else {
            throw BusinessException.invalidExpressionValue("集合", value);
        }
    }

    public static String constantToExpression(FieldValueDTO fieldValue) {
        if(fieldValue instanceof PrimitiveFieldValueDTO primitiveFieldValue) {
            Object value = primitiveFieldValue.getValue();
            if (value instanceof String str) {
                return "'" + str + "'";
            } else {
                return value.toString();
            }
        }
        if(fieldValue instanceof ReferenceFieldValueDTO refFieldValue) {
            return Constants.CONSTANT_ID_PREFIX + refFieldValue.getId();
        }
        if(fieldValue instanceof ArrayFieldValueDTO arrayFieldValue) {
            return "[" + NncUtils.join(arrayFieldValue.getElements(), ExpressionUtil::constantToExpression) + "]";
        }
        if(fieldValue instanceof ExpressionFieldValueDTO exprFieldValue) {
            return exprFieldValue.getExpression();
        }
        throw new InternalException("Can not convert value '" + fieldValue + "' to expression");
    }

    public static FieldValueDTO expressionToConstant(ConstantExpression expression) {
        return expression.getValue().toFieldValueDTO();
    }

    public static @Nullable String getAlias(Expression expression) {
        return expression instanceof AsExpression asExpression ? asExpression.getAlias() : null;
    }

    public InternalException notContextExpression(Expression expression, EvaluationContext context) {
        return new InternalException(expression + " is not a context expression of " + context);
    }

    public static long parseIdFromConstantVar(String var) {
        if(var.startsWith(Constants.CONSTANT_ID_PREFIX)) {
            return Long.parseLong(var.substring(Constants.CONSTANT_ID_PREFIX.length()));
        }
        throw new InternalException("Path item '" + var + "' does not represent an identity");
    }

}
