package tech.metavm.object.meta.generic;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.autograph.TranspileTestTools;
import tech.metavm.autograph.TranspileUtil;
import tech.metavm.autograph.TypeResolverImpl;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.expression.ConstantExpression;
import tech.metavm.flow.ExpressionValue;
import tech.metavm.flow.ValueNode;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.FieldBuilder;
import tech.metavm.object.meta.StandardTypes;
import tech.metavm.object.meta.mocks.GenericFoo;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;
import tech.metavm.util.TestUtils;

import java.util.List;

public class GenericTransformerTest extends TestCase {

    private InstanceContextFactory instanceContextFactory;

    @Override
    protected void setUp() throws Exception {
        var idProvider = new MockIdProvider();
        MockRegistry.setUp(idProvider);
        instanceContextFactory = TestUtils.getInstanceContextFactory(idProvider);
    }

    public void test() {
        IEntityContext context = instanceContextFactory.newContext(1L).getEntityContext();

        TypeResolverImpl typeResolver = new TypeResolverImpl(context);

        var psiClass = TranspileTestTools.getPsiClass(GenericFoo.class);
        var foo = (ClassType) typeResolver.resolve(TranspileUtil.createType(psiClass));
        var templateSetValue = foo.getFlowByCode("setValue");


        var typeParam = foo.getTypeParameters().get(0);
        var transformer = SubstitutorBuilder
                .newBuilder(foo.getTypeParameters(), List.of(StandardTypes.getStringType()), context.getGenericContext())
                .build();
        var transformed = transformer.substituteClassType(foo);
        var valueField = transformed.findFieldByCode("value");
        var getValueFlow = transformed.getFlowByCodeAndParamTypes("getValue", List.of());
        var setValueFlow = transformed.getFlowByCodeAndParamTypes("setValue", List.of(StandardTypes.getStringType()));

        Assert.assertNotNull(valueField);
        Assert.assertNotNull(getValueFlow);
        Assert.assertNotNull(setValueFlow);

        var value2 = FieldBuilder.newBuilder("value2", "value2", foo, typeParam).build();

        new ValueNode(
                null, "constant1", typeParam, templateSetValue.getRootScope().getLastNode(),
                templateSetValue.getRootScope(),
                new ExpressionValue(new ConstantExpression(InstanceUtils.longInstance(1L)))
        );

        var transformer2 = SubstitutorBuilder
                .newBuilder(foo.getTypeParameters(), List.of(StandardTypes.getStringType()), context.getGenericContext())
                .existing(transformed)
                .build();

        var transformed2 = transformer2.substituteClassType(foo);
        var getValueFlow2 = transformed2.getFlowByCodeAndParamTypes("getValue", List.of());
        var setValueFlow2 = transformed2.getFlowByCodeAndParamTypes("setValue", List.of(StandardTypes.getStringType()));

        Assert.assertSame(transformed, transformed2);
        Assert.assertSame(transformed, transformed2);
        Assert.assertSame(valueField, transformed2.findFieldByCode("value"));
        Assert.assertSame(getValueFlow, getValueFlow2);
        Assert.assertSame(setValueFlow, setValueFlow2);
        Assert.assertSame(setValueFlow.getParameters().get(0), setValueFlow2.getParameters().get(0));
        Assert.assertTrue(setValueFlow2.getRootScope().getLastNode() instanceof ValueNode);

        Assert.assertNotNull(transformed2.findFieldByCode(value2.getCode()));

    }
}