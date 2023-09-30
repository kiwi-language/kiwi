package tech.metavm.expression;

import junit.framework.TestCase;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;

public class ExpressionParserTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
//        MockRegistry.setUp(new MockIdProvider());
    }

    public void testTypeArguments() {
        String expr = "List<Map<Long,Map<String,Object>>>.size < 1 + a.num";
        ExpressionParser parser = new ExpressionParser(
                new ExpressionTokenizer(expr, 0),
                null
        );

        var expression = parser.preParse();
        System.out.println(expression.buildSelf(VarType.NAME));
    }

    public void testTypeArguments_2() {
        String expr = "While.索引 < LEN(Input.directCoupons)";
        ExpressionParser parser = new ExpressionParser(
                new ExpressionTokenizer(expr, 0),
                null
        );

        var expression = parser.preParse();
        System.out.println(expression.buildSelf(VarType.NAME));
    }

}