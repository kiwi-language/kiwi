package tech.metavm.object.instance.query;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.MockEntityContext;
import tech.metavm.expression.*;
import tech.metavm.mocks.Bar;
import tech.metavm.mocks.Baz;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;
import tech.metavm.view.ListView;

import java.util.List;

public class ExpressionEvaluatorTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(ExpressionEvaluatorTest.class);

    private MockIdProvider idProvider;

    @Override
    protected void setUp() throws Exception {
        MockRegistry.setUp(idProvider = new MockIdProvider());
    }

    public void testAllMatch() {
        ClassType fooType = MockRegistry.getClassType(Foo.class);
        String str = "AllMatch(巴子, AllMatch(巴巴巴巴, 编号=this.编号))";
        Expression expression = ExpressionParser.parse(
                str, new TypeParsingContext(fooType, id -> {throw new UnsupportedOperationException();})
        );

        LOGGER.info(expression.buildSelf(VarType.NAME));

        Foo foo = new Foo("Big Foo", new Bar("001"));
        foo.setBazList(List.of(
                new Baz(
                        List.of(
                                new Bar("002"),
                                new Bar("002")
                        )
                ),
                new Baz(
                        List.of(
                                new Bar("002"),
                                new Bar("002")
                        )
                )
        ));
        foo.setCode("002");

        IEntityContext context = newContext();

        ClassInstance fooInst = (ClassInstance) MockRegistry.getInstance(foo);
        Instance result = ExpressionEvaluator.evaluate(expression, fooInst, context);
        Assert.assertTrue(InstanceUtils.isTrue(result));
    }

    private IEntityContext newContext() {
        return new MockEntityContext(
                MockRegistry.getInstanceContext().getEntityContext(), idProvider, MockRegistry.getDefContext()
        );
    }

    public void testAllMatchListView() {
        ClassType fooType = MockRegistry.getClassType(Foo.class);
        ClassType listViewType = MockRegistry.getClassType(ListView.class);
        String str = "AllMatch(可见字段, 所属类型 = this.类型)";
        Expression expression = ExpressionParser.parse(
                str, new TypeParsingContext(listViewType)
        );

        ListView listView = new ListView("001", fooType);
        listView.setVisibleFields(List.of(
            MockRegistry.getField(Foo.class, "name")
        ));

        IEntityContext context = newContext();

        ClassInstance listViewInst = (ClassInstance) MockRegistry.getInstance(listView);
        Instance result = ExpressionEvaluator.evaluate(expression, listViewInst, context);
        Assert.assertTrue(InstanceUtils.isTrue(result));
    }

}