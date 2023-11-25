package tech.metavm.autograph;

import junit.framework.TestCase;
import tech.metavm.autograph.mocks.TypeReducerFoo;
import tech.metavm.expression.*;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;

public class TypeReducerTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockRegistry.setUp(new MockIdProvider());
    }

    public void test() {
        var fooType = MockRegistry.getClassType(TypeReducerFoo.class);
        var nameField = fooType.tryGetFieldByName("code");
        var amountField = fooType.tryGetFieldByName("amount");
        assertTrue(nameField.getType().isNullable());
        var nameFieldExpr = new PropertyExpression(
                new ThisExpression(fooType),
                nameField
        );
        var amountFieldExpr = new PropertyExpression(new ThisExpression(fooType), amountField);
        Expression expression = new BinaryExpression(
                BinaryOperator.OR,
                new BinaryExpression(
                        BinaryOperator.AND,
                        new BinaryExpression(
                                BinaryOperator.NE,
                                nameFieldExpr,
                                ExpressionUtil.nullExpression()
                        ),
                        new BinaryExpression(
                                BinaryOperator.GT,
                                amountFieldExpr,
                                ExpressionUtil.constantLong(0)
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
                                ExpressionUtil.constantLong(-1L)
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