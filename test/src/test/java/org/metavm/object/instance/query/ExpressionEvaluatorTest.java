package org.metavm.object.instance.query;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.expression.Expression;
import org.metavm.expression.ExpressionParser;
import org.metavm.expression.InstanceEvaluationContext;
import org.metavm.expression.TypeParsingContext;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.core.mocks.MockInstanceRepository;
import org.metavm.object.type.IndexedTypeDefProvider;
import org.metavm.object.type.mocks.MockTypeDefRepository;
import org.metavm.util.Instances;
import org.metavm.util.MockUtils;
import org.metavm.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ExpressionEvaluatorTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(ExpressionEvaluatorTest.class);

    private InstanceProvider instanceProvider;
    private IndexedTypeDefProvider typeDefProvider;

    @Override
    protected void setUp() throws Exception {
        TestUtils.ensureStringKlassInitialized();
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
                                new ArrayInstance(fooTypes.barChildArrayType()).getReference(),
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
                                                                        .buildAndGetReference(),
                                                                ClassInstanceBuilder.newBuilder(fooTypes.barType().getType())
                                                                        .data(Map.of(
                                                                                fooTypes.barCodeField(), Instances.stringInstance("001")
                                                                        ))
                                                                        .buildAndGetReference()
                                                        )).getReference()
                                                ))
                                                .buildAndGetReference()
                                )).getReference()
                        )
                )
                .build();
        Value result = expression.evaluate(new InstanceEvaluationContext(fooInst));
        Assert.assertEquals(Instances.trueInstance(), result);
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
                                        .buildAndGetReference(),
                                ClassInstanceBuilder.newBuilder(fooTypes.barType().getType())
                                        .data(Map.of(
                                                fooTypes.barCodeField(), Instances.stringInstance("001")
                                        ))
                                        .buildAndGetReference()
                        )).getReference(),
                        fooTypes.fooBazListField(),
                        new ArrayInstance(fooTypes.bazArrayType()).getReference()
                ))
                .build();
        String str = "allmatch(bars, code = this.code)";
        Expression expression = ExpressionParser.parse(
                str, new TypeParsingContext(instanceProvider, typeDefProvider, fooType)
        );
        Value result = expression.evaluate(new InstanceEvaluationContext(foo));
        Assert.assertEquals(Instances.trueInstance(), result);
    }

}