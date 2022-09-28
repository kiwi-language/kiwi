package tech.metavm.object.instance.query;

import tech.metavm.object.meta.Field;

public class Expression {

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

    public static Expression fieldStartsWith(Field field, String value) {
        return new BinaryExpression(
                Operator.STARTS_WITH,
                new FieldExpression(field.getName(), field),
                new ConstantExpression(value)
        );
    }

    public static Expression fieldLike(Field field, String value) {
        return new BinaryExpression(
                Operator.LIKE,
                new FieldExpression(field.getName(), field),
                new ConstantExpression(value)
        );
    }

    public static Expression fieldEq(Field field, Object value) {
        return new BinaryExpression(
                Operator.EQ,
                new FieldExpression(field.getName(), field),
                new ConstantExpression(value)
        );
    }

}
