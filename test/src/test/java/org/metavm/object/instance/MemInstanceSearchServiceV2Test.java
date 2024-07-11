package org.metavm.object.instance;

import junit.framework.TestCase;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.expression.Expressions;
import org.metavm.expression.ThisExpression;
import org.metavm.object.instance.search.SearchQuery;
import org.metavm.util.MockUtils;
import org.metavm.util.TestConstants;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MemInstanceSearchServiceV2Test extends TestCase {

    private MemInstanceSearchServiceV2 memInstanceSearchServiceV2;

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
        memInstanceSearchServiceV2 = new MemInstanceSearchServiceV2();
    }

    public void test() {
        var fooTypes = MockUtils.createFooTypes(true);
        var foo = MockUtils.createFoo(fooTypes, true);
        var fooType = fooTypes.fooType().getType();
        memInstanceSearchServiceV2.bulk(TestConstants.APP_ID, List.of(foo), List.of());
        var result = memInstanceSearchServiceV2.search(new SearchQuery(
                TestConstants.APP_ID,
                Set.of(fooType.toExpression()),
                Expressions.and(
                        Expressions.and(
                                Expressions.eq(
                                        Expressions.property(
                                                new ThisExpression(fooType),
                                                fooTypes.fooNameField()
                                        ),
                                        Expressions.constant(foo.getField(fooTypes.fooNameField()))
                                ),
                                Expressions.eq(
                                        Expressions.property(
                                                new ThisExpression(fooType),
                                                fooTypes.fooQuxField()
                                        ),
                                        Expressions.constant(foo.getField(fooTypes.fooQuxField()))
                                )
                        ),
                        Expressions.eq(
                                Expressions.property(
                                        new ThisExpression(fooType),
                                        fooTypes.fooBazListField()
                                ),
                                Expressions.constant(foo.getField(fooTypes.fooBazListField()).resolveArray().get(0))
                        )
                ),
                false,
                1,
                20,
                0
        ));
        assertEquals(1, result.total());
        assertEquals(Objects.requireNonNull(foo.tryGetId()), result.data().get(0));
    }

    public void testSearchingSuperClassField() {
        var livingBeingTypes = MockUtils.createLivingBeingTypes(true);
        var humanType = livingBeingTypes.humanType().getType();
        var human = MockUtils.createHuman(livingBeingTypes, true);
        memInstanceSearchServiceV2.bulk(TestConstants.APP_ID, List.of(human), List.of());
        var result = memInstanceSearchServiceV2.search(new SearchQuery(
                TestConstants.APP_ID,
                Set.of(humanType.toExpression()),
                Expressions.eq(
                        Expressions.property(
                                new ThisExpression(humanType),
                                livingBeingTypes.livingBeingAgeField()
                        ),
                        Expressions.constant(human.getField(livingBeingTypes.livingBeingAgeField()))
                ),
                false,
                1,
                20,
                0
        ));
        assertEquals(1, result.total());
        assertEquals(Objects.requireNonNull(human.tryGetId()), result.data().get(0));

    }

}