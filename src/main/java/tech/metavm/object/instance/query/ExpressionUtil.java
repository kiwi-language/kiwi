package tech.metavm.object.instance.query;

import tech.metavm.object.instance.*;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.BusinessException;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ValueUtil;

import java.util.Collection;

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

    public static Expression fieldStartsWith(Field field, PrimitiveInstance strInstance) {
        return new BinaryExpression(
                Operator.STARTS_WITH,
                new FieldExpression(thisObject(field.getDeclaringType()), field),
                new ConstantExpression(strInstance)
        );
    }

    public static Expression fieldLike(Field field, PrimitiveInstance strInstance) {
        return new BinaryExpression(
                Operator.LIKE,
                new FieldExpression(thisObject(field.getDeclaringType()), field),
                new ConstantExpression(strInstance)
        );
    }

    public static Expression fieldEq(Field field, Instance value) {
        return new BinaryExpression(
                Operator.EQ,
                new FieldExpression(thisObject(field.getDeclaringType()), field),
                new ConstantExpression(value)
        );
    }

    public static Expression fieldIn(Field field, Collection<? extends Instance> values) {
        return new BinaryExpression(
                Operator.IN,
                new FieldExpression(thisObject(field.getDeclaringType()), field),
                new ArrayExpression(
                        NncUtils.map(values, ConstantExpression::new)
                )
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

    public static boolean isConstantTrue(Expression expression) {
        if(expression instanceof ConstantExpression constantExpression) {
            return Boolean.TRUE.equals(constantExpression.getValue());
        }
        else {
            return false;
        }
    }

    public static LongInstance castInteger(Instance value) {
        if(value instanceof LongInstance longInstance) {
            return longInstance;
        }
        else if(value instanceof IntInstance intInstance) {
            return InstanceUtils.convertToLong(intInstance);
        }
        else {
            throw BusinessException.invalidExpressionValue("整数", value);
        }
    }

    public static DoubleInstance castFloat(Instance value) {
        if(value instanceof DoubleInstance doubleInstance) {
            return doubleInstance;
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
            throw BusinessException.invalidExpressionValue("文本", value);
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

}
