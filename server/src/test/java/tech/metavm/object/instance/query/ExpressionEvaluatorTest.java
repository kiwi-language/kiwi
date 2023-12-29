package tech.metavm.object.instance.query;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.expression.Expression;
import tech.metavm.expression.ExpressionParser;
import tech.metavm.expression.InstanceEvaluationContext;
import tech.metavm.expression.TypeParsingContext;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.mocks.Bar;
import tech.metavm.mocks.Baz;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.InstanceProvider;
import tech.metavm.object.instance.core.mocks.MockInstanceRepository;
import tech.metavm.object.type.ArrayTypeProvider;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.IndexedTypeProvider;
import tech.metavm.object.type.mocks.TypeProviders;
import tech.metavm.object.type.mocks.MockArrayTypeProvider;
import tech.metavm.object.type.mocks.MockTypeRepository;
import tech.metavm.util.Instances;
import tech.metavm.util.MockRegistry;
import tech.metavm.view.ListView;

import java.util.List;

public class ExpressionEvaluatorTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(ExpressionEvaluatorTest.class);

    private InstanceProvider instanceProvider;
    private IndexedTypeProvider typeProvider;
    private ArrayTypeProvider arrayTypeProvider;
    private ParameterizedFlowProvider parameterizedFlowProvider;

    @Override
    protected void setUp() throws Exception {
        typeProvider = new MockTypeRepository();
        instanceProvider = new MockInstanceRepository();
        arrayTypeProvider = new MockArrayTypeProvider();
        parameterizedFlowProvider = new TypeProviders().parameterizedFlowProvider;
    }

    public void testAllMatch() {
        ClassType fooType = MockRegistry.getClassType(Foo.class);
        String str = "AllMatch(巴子, AllMatch(巴巴巴巴, 编号=this.编号))";
        Expression expression = ExpressionParser.parse(
                str, new TypeParsingContext(
                        instanceProvider,
                        typeProvider,
                        arrayTypeProvider,
                        fooType
                )
        );

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

        ClassInstance fooInst = (ClassInstance) MockRegistry.getInstance(foo);
        Instance result = expression.evaluate(new InstanceEvaluationContext(fooInst, parameterizedFlowProvider));
        Assert.assertTrue(Instances.isTrue(result));
    }

    public void testAllMatchListView() {
        ClassType fooType = MockRegistry.getClassType(Foo.class);
        ClassType listViewType = MockRegistry.getClassType(ListView.class);
        String str = "AllMatch(可见字段, 所属类型 = this.类型)";
        Expression expression = ExpressionParser.parse(
                str, new TypeParsingContext(instanceProvider, typeProvider, arrayTypeProvider, listViewType)
        );

        ListView listView = new ListView("001", fooType);
        listView.setVisibleFields(List.of(
                MockRegistry.getField(Foo.class, "name")
        ));

        ClassInstance listViewInst = (ClassInstance) MockRegistry.getInstance(listView);
        Instance result = expression.evaluate(new InstanceEvaluationContext(listViewInst, parameterizedFlowProvider));
        Assert.assertTrue(Instances.isTrue(result));
    }

}