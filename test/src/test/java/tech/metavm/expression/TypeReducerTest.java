package tech.metavm.expression;

import junit.framework.TestCase;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.entity.StandardTypes;
import tech.metavm.object.type.ClassTypeBuilder;
import tech.metavm.object.type.FieldBuilder;
import tech.metavm.object.type.UnionType;

import java.util.Set;

public class TypeReducerTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void test() {
        var fooType = ClassTypeBuilder.newBuilder("Foo", "Foo").build();
        var nullableStringType = new UnionType(null,
                Set.of(StandardTypes.getStringType(), StandardTypes.getNullType()));
        var nameField = FieldBuilder.newBuilder("code", "code", fooType, nullableStringType)
                .build();
        var amountField = FieldBuilder.newBuilder("amount", "amount", fooType, StandardTypes.getLongType())
                .build();
        assertTrue(nameField.getType().isNullable());
        var nameFieldExpr = new PropertyExpression(
                new ThisExpression(fooType.getType()),
                nameField
        );
        var amountFieldExpr = new PropertyExpression(new ThisExpression(fooType.getType()), amountField);
        Expression expression = new BinaryExpression(
                BinaryOperator.OR,
                new BinaryExpression(
                        BinaryOperator.AND,
                        new BinaryExpression(
                                BinaryOperator.NE,
                                nameFieldExpr,
                                Expressions.nullExpression()
                        ),
                        new BinaryExpression(
                                BinaryOperator.GT,
                                amountFieldExpr,
                                Expressions.constantLong(0)
                        )
                ),
                new BinaryExpression(
                        BinaryOperator.AND,
                        new UnaryExpression(
                                UnaryOperator.IS_NOT_NULL,
                                nameFieldExpr
                        ),
                        new BinaryExpression(
                                BinaryOperator.EQ,
                                amountFieldExpr,
                                Expressions.constantLong(-1L)
                        )
                )
        );
        TypeNarrower typeReducer = new TypeNarrower(Expression::getType);
        var reduceResult = typeReducer.narrowType(expression);
        var reducedType = reduceResult.getType(nameFieldExpr);
        assertNotNull(reducedType);
        assertEquals(nameField.getType().getUnderlyingType(), reducedType);
    }

}