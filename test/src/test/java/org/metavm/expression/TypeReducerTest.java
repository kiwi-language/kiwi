package org.metavm.expression;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.entity.StandardTypes;
import org.metavm.object.type.KlassBuilder;
import org.metavm.object.type.FieldBuilder;
import org.metavm.object.type.UnionType;

import java.util.Set;

public class TypeReducerTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(TypeReducerTest.class);

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void test() {
        var fooType = KlassBuilder.newBuilder("Foo", "Foo").build();
        var nullableStringType = new UnionType(
                Set.of(StandardTypes.getStringType(), StandardTypes.getNullType()));
        var nameField = FieldBuilder.newBuilder("code", "code", fooType, nullableStringType)
                .build();
        var amountField = FieldBuilder.newBuilder("amount", "amount", fooType, StandardTypes.getLongType())
                .build();
        assertTrue(nameField.getType().isNullable());
        var nameFieldExpr = new PropertyExpression(
                new ThisExpression(fooType.getType()),
                nameField.getRef()
        );
        var amountFieldExpr = new PropertyExpression(new ThisExpression(fooType.getType()), amountField.getRef());
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
        logger.info("{}", reduceResult.toMap());
        var reducedType = reduceResult.getType(nameFieldExpr);
        assertNotNull(reducedType);
        assertEquals(nameField.getType().getUnderlyingType(), reducedType);
    }

}