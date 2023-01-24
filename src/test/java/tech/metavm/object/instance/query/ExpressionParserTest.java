package tech.metavm.object.instance.query;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.entity.MemInstanceStore;
import tech.metavm.mocks.Foo;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;
import tech.metavm.util.TestUtils;

import static tech.metavm.util.TestConstants.TENANT_ID;

public class ExpressionParserTest extends TestCase {

    private InstanceContextFactory instanceContextFactory;

    @Override
    protected void setUp() throws Exception {
        MockIdProvider idProvider = new MockIdProvider();
        MemInstanceStore instanceStore = new MemInstanceStore();
        MockRegistry.setUp(idProvider, instanceStore);
        instanceContextFactory = TestUtils.getInstanceContextFactory(idProvider, instanceStore);
    }

    public void testIn() {
        ClassType fooType = MockRegistry.getClassType(Foo.class);
        IInstanceContext context = instanceContextFactory.newContext(TENANT_ID);
        String exprString = "名称 IN 'Big Foo'";
        Expression expression = ExpressionParser.parse(
                exprString,
                new TypeParsingContext(fooType, context)
        );
        Assert.assertNotNull(expression);
        Assert.assertEquals(exprString, expression.buildSelf(VarType.NAME));
    }

}