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
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.core.mocks.MockInstanceRepository;
import tech.metavm.object.type.IndexedTypeDefProvider;
import tech.metavm.object.type.mocks.MockTypeDefRepository;
import tech.metavm.util.Instances;
import tech.metavm.util.MockUtils;

import java.util.List;
import java.util.Map;

public class ExpressionEvaluatorTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(ExpressionEvaluatorTest.class);

    private InstanceProvider instanceProvider;
    private IndexedTypeDefProvider typeDefProvider;

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
        typeDefProvider = new MockTypeDefRepository();
        instanceProvider = new MockInstanceRepository();
    }

    public void testAllMatch() {
        var fooTypes = MockUtils.createFooTypes(true);
        var fooType = fooTypes.fooType();
        String str = "allmatch(bazList, allmatch(bars, code=this.code))";
        Expression expression = ExpressionParser.parse(
                str, new TypeParsingContext(
                        instanceProvider,
                        typeDefProvider,
                        fooType
                )
        );

        ClassInstance fooInst = ClassInstanceBuilder.newBuilder(fooType.getType())
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
                                        ClassInstanceBuilder.newBuilder(fooTypes.bazType().getType())
                                                .data(Map.of(
                                                        fooTypes.bazBarsField(),
                                                        new ArrayInstance(fooTypes.barArrayType(), List.of(
                                                                ClassInstanceBuilder.newBuilder(fooTypes.barType().getType())
                                                                        .data(Map.of(
                                                                                fooTypes.barCodeField(), Instances.stringInstance("001")
                                                                        ))
                                                                        .build(),
                                                                ClassInstanceBuilder.newBuilder(fooTypes.barType().getType())
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
        Instance result = expression.evaluate(new InstanceEvaluationContext(fooInst));
        Assert.assertTrue(Instances.isTrue(result));
    }

    public void testAllMatchListView() {
        var fooTypes = MockUtils.createFooTypes(true);
        var fooType = fooTypes.fooType();
        var foo = ClassInstanceBuilder.newBuilder(fooType.getType())
                .data(Map.of(
                        fooTypes.fooNameField(), Instances.stringInstance("foo"),
                        fooTypes.fooCodeField(), Instances.stringInstance("001"),
                        fooTypes.fooBarsField(),
                        new ArrayInstance(fooTypes.barChildArrayType(), List.of(
                                ClassInstanceBuilder.newBuilder(fooTypes.barType().getType())
                                        .data(Map.of(
                                                fooTypes.barCodeField(), Instances.stringInstance("001")
                                        ))
                                        .build(),
                                ClassInstanceBuilder.newBuilder(fooTypes.barType().getType())
                                        .data(Map.of(
                                                fooTypes.barCodeField(), Instances.stringInstance("001")
                                        ))
                                        .build()
                        )),
                        fooTypes.fooBazListField(),
                        new ArrayInstance(fooTypes.bazArrayType())
                ))
                .build();
        String str = "allmatch(bars, code = this.code)";
        Expression expression = ExpressionParser.parse(
                str, new TypeParsingContext(instanceProvider, typeDefProvider, fooType)
        );
        Instance result = expression.evaluate(new InstanceEvaluationContext(foo));
        Assert.assertTrue(Instances.isTrue(result));
    }

}