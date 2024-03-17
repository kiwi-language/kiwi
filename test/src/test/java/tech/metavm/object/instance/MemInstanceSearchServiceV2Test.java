package tech.metavm.object.instance;

import junit.framework.TestCase;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.expression.Expressions;
import tech.metavm.expression.ThisExpression;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.search.SearchQuery;
import tech.metavm.util.MockUtils;
import tech.metavm.util.TestConstants;

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
        memInstanceSearchServiceV2.bulk(TestConstants.APP_ID, List.of(foo), List.of());
        var result = memInstanceSearchServiceV2.search(new SearchQuery(
                TestConstants.APP_ID,
                Set.of(fooTypes.fooType().getPhysicalId()),
                Expressions.and(
                        Expressions.and(
                                Expressions.eq(
                                        Expressions.property(
                                                new ThisExpression(fooTypes.fooType()),
                                                fooTypes.fooNameField()
                                        ),
                                        Expressions.constant(foo.getField(fooTypes.fooNameField()))
                                ),
                                Expressions.eq(
                                        Expressions.property(
                                                new ThisExpression(fooTypes.fooType()),
                                                fooTypes.fooQuxField()
                                        ),
                                        Expressions.constant(foo.getField(fooTypes.fooQuxField()))
                                )
                        ),
                        Expressions.eq(
                                Expressions.property(
                                        new ThisExpression(fooTypes.fooType()),
                                        fooTypes.fooBazListField()
                                ),
                                Expressions.constant(((ArrayInstance) foo.getField(fooTypes.fooBazListField())).get(0))
                        )
                ),
                false,
                1,
                20,
                0
        ));
        assertEquals(1, result.total());
        assertEquals(Objects.requireNonNull(foo.getId()), result.data().get(0));
    }

    public void testSearchingSuperClassField() {
        var livingBeingTypes = MockUtils.createLivingBeingTypes(true);
        var human = MockUtils.createHuman(livingBeingTypes, true);
        memInstanceSearchServiceV2.bulk(TestConstants.APP_ID, List.of(human), List.of());
        var result = memInstanceSearchServiceV2.search(new SearchQuery(
                TestConstants.APP_ID,
                Set.of(livingBeingTypes.humanType().getPhysicalId()),
                Expressions.eq(
                        Expressions.property(
                                new ThisExpression(livingBeingTypes.humanType()),
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
        assertEquals(Objects.requireNonNull(human.getId()), result.data().get(0));

    }

}