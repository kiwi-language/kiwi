package tech.metavm.object.instance.query;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.entity.MemIndexEntryMapper;
import tech.metavm.object.instance.MockInstanceLogService;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.entity.MemInstanceStore;
import tech.metavm.expression.*;
import tech.metavm.mocks.Foo;
import tech.metavm.object.type.ClassType;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;
import tech.metavm.util.TestUtils;
import tech.metavm.view.ListView;

import static tech.metavm.util.TestConstants.APP_ID;

public class ExpressionTypeResolverTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(ExpressionTypeResolverTest.class);

    private EntityContextFactory entityContextFactory;

    @Override
    protected void setUp() throws Exception {
        MockIdProvider idProvider = new MockIdProvider();
        MemInstanceStore instanceStore = new MemInstanceStore();
        MockRegistry.setUp(idProvider, instanceStore);
        entityContextFactory = TestUtils.getEntityContextFactory(idProvider, instanceStore, new MockInstanceLogService(), new MemIndexEntryMapper());
    }

    public void testIn() {
        ClassType fooType = MockRegistry.getClassType(Foo.class);
        var context = entityContextFactory.newContext(APP_ID);
        String exprString = "名称 in 'Big Foo'";
        Expression expression = ExpressionParser.parse(
                exprString,
                TypeParsingContext.create(fooType, context)
        );
        Assert.assertNotNull(expression);
        Assert.assertEquals(exprString, expression.build(VarType.NAME));
    }

    public void testAllMatch() {
        ClassType listViewType = MockRegistry.getClassType(ListView.class);
        var context = entityContextFactory.newContext(APP_ID);
        String str = "AllMatch(可见字段, 所属类型=this.类型)";
        Expression expression = ExpressionParser.parse(
                str, TypeParsingContext.create(listViewType, context)
        );
        Assert.assertTrue(expression instanceof AllMatchExpression);
        LOGGER.info(expression.build(VarType.NAME));
    }

}