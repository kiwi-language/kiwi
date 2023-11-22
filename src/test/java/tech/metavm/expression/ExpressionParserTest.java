package tech.metavm.expression;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.object.type.ArrayKind;
import tech.metavm.object.type.ClassBuilder;
import tech.metavm.object.type.FieldBuilder;
import tech.metavm.entity.StandardTypes;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;

import java.util.Objects;

public class ExpressionParserTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockRegistry.setUp(new MockIdProvider());
    }

    public void testArray() {
        ExpressionParser parser = new ExpressionParser("[1, 2]", null);
        var expr = parser.antlrPreparse();
        Assert.assertTrue(expr instanceof ArrayExpression);
    }


    public void testArrayAccess() {
        ExpressionParser parser = new ExpressionParser("list[item.index]", null);
        var expr = parser.antlrPreparse();
        Assert.assertTrue(expr instanceof ArrayAccessExpression);
    }

    public void testTypeArguments() {
        try (var context = MockRegistry.newEntityContext(1L)) {
            var fooType = ClassBuilder.newBuilder("Foo", null).build();
            FieldBuilder.newBuilder("num", null, fooType, StandardTypes.getLongType()).build();

            var type = ClassBuilder.newBuilder("List<Map<Long,Map<String,Object>>>", null).build();
            FieldBuilder.newBuilder("size", null, type, StandardTypes.getLongType()).build();
            FieldBuilder.newBuilder("a", null, type, fooType).build();

            context.bind(type);
            String expr = "List<Map<Long,Map<String,Object>>>.size < 1 + a.num";
            ExpressionParser parser = new ExpressionParser(expr,
                    new TypeParsingContext(type, Objects.requireNonNull(context.getInstanceContext())));
            var expression = parser.parse(null);
            System.out.println(expression.buildSelf(VarType.NAME));
        }
    }

    public void testTypeArguments_2() {
        try (var context = MockRegistry.newEntityContext(1L)) {
            var couponType = ClassBuilder.newBuilder("Coupon", null).build();
            var couponArrayType = context.getArrayType(couponType, ArrayKind.READ_WRITE);

            var inputType = ClassBuilder.newBuilder("Input", null).build();
            FieldBuilder.newBuilder("directCoupons", null, inputType, couponArrayType).build();

            var whileType = ClassBuilder.newBuilder("WhileOutput", null).build();
            FieldBuilder.newBuilder("索引", null, whileType, StandardTypes.getLongType()).build();

            var type = ClassBuilder.newBuilder("Foo", null).build();
            FieldBuilder.newBuilder("While", null, type, whileType).build();
            FieldBuilder.newBuilder("Input", null, type, inputType).build();

            String expr = "While.索引 < LEN(Input.directCoupons)";
            ExpressionParser parser = new ExpressionParser(expr,
                    new TypeParsingContext(type, Objects.requireNonNull(context.getInstanceContext())));
            var expression = parser.parse(null);
            System.out.println(expression.buildSelf(VarType.NAME));
        }
    }

}