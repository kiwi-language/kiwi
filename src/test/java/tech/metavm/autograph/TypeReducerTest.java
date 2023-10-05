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
        var nameField = fooType.getFieldByName("code");
        var amountField = fooType.getFieldByName("amount");
        assertTrue(nameField.getType().isNullable());
        var nameFieldExpr = new FieldExpression(
                new ThisExpression(fooType),
                nameField
        );
        var amountFieldExpr = new FieldExpression(new ThisExpression(fooType), amountField);
        Expression expression = new BinaryExpression(
                Operator.OR,
                new BinaryExpression(
                        Operator.AND,
                        new BinaryExpression(
                                Operator.NE,
                                nameFieldExpr,
                                ExpressionUtil.nullExpression()
                        ),
                        new BinaryExpression(
                                Operator.GT,
                                amountFieldExpr,
                                ExpressionUtil.constantLong(0)
                        )
                ),
                new BinaryExpression(
                        Operator.AND,
                        new UnaryExpression(
                                Operator.IS_NOT_NULL,
                                nameFieldExpr
                        ),
                        new BinaryExpression(
                                Operator.EQ,
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