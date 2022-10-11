package tech.metavm.object.instance.query;

import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.BusinessException;
import tech.metavm.util.ValueUtil;

import java.util.Collection;

public class ExpressionUtil {

    public static Expression thisObject(Type type) {
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

    public static Expression fieldStartsWith(Field field, String value) {
        return new BinaryExpression(
                Operator.STARTS_WITH,
                new FieldExpression(thisObject(field.getOwner()), field),
                new ConstantExpression(value)
        );
    }

    public static Expression fieldLike(Field field, String value) {
        return new BinaryExpression(
                Operator.LIKE,
                new FieldExpression(thisObject(field.getOwner()), field),
                new ConstantExpression(value)
        );
    }

    public static Expression fieldEq(Field field, Object value) {
        return new BinaryExpression(
                Operator.EQ,
                new FieldExpression(thisObject(field.getOwner()), field),
                new ConstantExpression(value)
        );
    }

    public static Expression subtract(Expression first, Expression second) {
        return new BinaryExpression(Operator.SUBTRACT, first, second);
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

    public static Long castInteger(Object value) {
        if(ValueUtil.isInteger(value)) {
            return ((Number) value).longValue();
        }
        else {
            throw BusinessException.invalidExpressionValue("整数", value);
        }
    }

    public static Double castFloat(Object value) {
        if(ValueUtil.isNumber(value)) {
            return ((Number) value).doubleValue();
        }
        else {
            throw BusinessException.invalidExpressionValue("数值", value);
        }
    }

    public static Boolean castBoolean(Object value) {
        if(ValueUtil.isBoolean(value)) {
            return (Boolean) value;
        }
        else {
            throw BusinessException.invalidExpressionValue("布尔", value);
        }
    }

    public static String castString(Object value) {
        if(ValueUtil.isString(value)) {
            return (String) value;
        }
        else {
            throw BusinessException.invalidExpressionValue("文本", value);
        }
    }

    public static Collection<Object> castCollection(Object value) {
        if(ValueUtil.isCollection(value)) {
            return (Collection<Object>) value;
        }
        else {
            throw BusinessException.invalidExpressionValue("集合", value);
        }
    }

}
