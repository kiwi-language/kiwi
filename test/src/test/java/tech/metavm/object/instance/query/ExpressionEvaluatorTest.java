package tech.metavm.object.instance.query;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.expression.Expression;
import tech.metavm.expression.ExpressionParser;
import tech.metavm.expression.InstanceEvaluationContext;
import tech.metavm.expression.TypeParsingContext;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.core.mocks.MockInstanceRepository;
import tech.metavm.object.type.ArrayTypeProvider;
import tech.metavm.object.type.IndexedTypeProvider;
import tech.metavm.object.type.UnionTypeProvider;
import tech.metavm.object.type.mocks.MockArrayTypeProvider;
import tech.metavm.object.type.mocks.MockTypeRepository;
import tech.metavm.object.type.mocks.MockUnionTypeProvider;
import tech.metavm.object.type.mocks.TypeProviders;
import tech.metavm.util.Instances;
import tech.metavm.util.MockUtils;

import java.util.List;
import java.util.Map;

public class ExpressionEvaluatorTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(ExpressionEvaluatorTest.class);

    private InstanceProvider instanceProvider;
    private IndexedTypeProvider typeProvider;
    private ArrayTypeProvider arrayTypeProvider;
    private UnionTypeProvider unionTypeProvider;
    private ParameterizedFlowProvider parameterizedFlowProvider;

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
        typeProvider = new MockTypeRepository();
        instanceProvider = new MockInstanceRepository();
        arrayTypeProvider = new MockArrayTypeProvider();
        unionTypeProvider = new MockUnionTypeProvider();
        parameterizedFlowProvider = new TypeProviders().parameterizedFlowProvider;
    }

    public void testAllMatch() {
        var fooTypes = MockUtils.createFooTypes(true);
        var fooType = fooTypes.fooType();
        String str = "allmatch(巴子列表, allmatch(巴列表, 编号=this.编号))";
        Expression expression = ExpressionParser.parse(
                str, new TypeParsingContext(
                        instanceProvider,
                        typeProvider,
                        arrayTypeProvider,
                        unionTypeProvider,
                        fooType
                )
        );

        ClassInstance fooInst = ClassInstanceBuilder.newBuilder(fooType)
                .data(
                        Map.of(
                                fooTypes.fooNameField(),
                                Instances.stringInstance("foo"),
                                fooTypes.fooCodeField(),
                                Instances.stringInstance("001"),
                                fooTypes.fooBarsField(),
                                new ArrayInstance(fooTypes.barChildArrayType()),
                                fooTypes.fooBazListField(),
                                new ArrayInstance(fooTypes.bazArrayType(), List.of(
                                        ClassInstanceBuilder.newBuilder(fooTypes.bazType())
                                                .data(Map.of(
                                                        fooTypes.bazBarsField(),
                                                        new ArrayInstance(fooTypes.barArrayType(), List.of(
                                                                ClassInstanceBuilder.newBuilder(fooTypes.barType())
                                                                        .data(Map.of(
                                                                                fooTypes.barCodeField(), Instances.stringInstance("001")
                                                                        ))
                                                                        .build(),
                                                                ClassInstanceBuilder.newBuilder(fooTypes.barType())
                                                                        .data(Map.of(
                                                                                fooTypes.barCodeField(), Instances.stringInstance("001")
                                                                        ))
                                                                        .build()
                                                        ))
                                                ))
                                                .build()
                                ))
                        )
                )
                .build();
        Instance result = expression.evaluate(new InstanceEvaluationContext(fooInst, parameterizedFlowProvider));
        Assert.assertTrue(Instances.isTrue(result));
    }

    public void testAllMatchListView() {
        var fooTypes = MockUtils.createFooTypes(true);
        var fooType = fooTypes.fooType();
        var foo = ClassInstanceBuilder.newBuilder(fooType)
                .data(Map.of(
                        fooTypes.fooNameField(), Instances.stringInstance("foo"),
                        fooTypes.fooCodeField(), Instances.stringInstance("001"),
                        fooTypes.fooBarsField(),
                        new ArrayInstance(fooTypes.barChildArrayType(), List.of(
                                ClassInstanceBuilder.newBuilder(fooTypes.barType())
                                        .data(Map.of(
                                                fooTypes.barCodeField(), Instances.stringInstance("001")
                                        ))
                                        .build(),
                                ClassInstanceBuilder.newBuilder(fooTypes.barType())
                                        .data(Map.of(
                                                fooTypes.barCodeField(), Instances.stringInstance("001")
                                        ))
                                        .build()
                        )),
                        fooTypes.fooBazListField(),
                        new ArrayInstance(fooTypes.bazArrayType())
                ))
                .build();
        String str = "allmatch(巴列表, 编号 = this.编号)";
        Expression expression = ExpressionParser.parse(
                str, new TypeParsingContext(instanceProvider, typeProvider, arrayTypeProvider, unionTypeProvider, fooType)
        );
        Instance result = expression.evaluate(new InstanceEvaluationContext(foo, parameterizedFlowProvider));
        Assert.assertTrue(Instances.isTrue(result));
    }

}