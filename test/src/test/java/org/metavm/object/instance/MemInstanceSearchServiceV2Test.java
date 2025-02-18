package org.metavm.object.instance;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.instance.search.AndSearchCondition;
import org.metavm.object.instance.search.MatchSearchCondition;
import org.metavm.object.instance.search.SearchQuery;
import org.metavm.util.MockUtils;
import org.metavm.util.TestConstants;
import org.metavm.util.TestUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class MemInstanceSearchServiceV2Test extends TestCase {

    private MemInstanceSearchServiceV2 memInstanceSearchServiceV2;
    private long nextTreeId;

    @Override
    protected void setUp() throws Exception {
        TestUtils.ensureStringKlassInitialized();
        MockStandardTypesInitializer.init();
        memInstanceSearchServiceV2 = new MemInstanceSearchServiceV2();
    }

    public void test() {
        var fooTypes = MockUtils.createFooTypes(true);
        var foo = MockUtils.createFoo(fooTypes, this::nextRootId);
        var fooType = fooTypes.fooType().getType();
        memInstanceSearchServiceV2.bulk(TestConstants.APP_ID, List.of(foo), List.of());
        var result = memInstanceSearchServiceV2.search(new SearchQuery(
                TestConstants.APP_ID,
                Set.of(fooType.toExpression()),
                new AndSearchCondition(
                        List.of(
                                new AndSearchCondition(
                                        List.of(
                                                new MatchSearchCondition(
                                                        fooTypes.fooNameField().getEsField(),
                                                        foo.getField(fooTypes.fooNameField())
                                                ),
                                                new MatchSearchCondition(
                                                        fooTypes.fooQuxField().getEsField(),
                                                        foo.getField(fooTypes.fooQuxField())
                                                )
                                        )
                                ),
                                new MatchSearchCondition(
                                        fooTypes.fooBazListField().getEsField(),
                                        foo.getField(fooTypes.fooBazListField()).resolveArray().getFirst()
                                )
                        )
                ),
                false,
                1,
                20,
                0
        ));
        assertEquals(1, result.total());
        assertEquals(Objects.requireNonNull(foo.tryGetId()), result.data().getFirst());
    }

    public void testSearchingSuperClassField() {
        var livingBeingTypes = MockUtils.createLivingBeingTypes(true);
        var humanType = livingBeingTypes.humanType().getType();
        var human = MockUtils.createHuman(livingBeingTypes, nextRootId());
        memInstanceSearchServiceV2.bulk(TestConstants.APP_ID, List.of(human), List.of());
        var result = memInstanceSearchServiceV2.search(new SearchQuery(
                TestConstants.APP_ID,
                Set.of(humanType.toExpression()),
                new MatchSearchCondition(
                        livingBeingTypes.livingBeingAgeField().getEsField(),
                        human.getField(livingBeingTypes.livingBeingAgeField())
                ),
                false,
                1,
                20,
                0
        ));
        assertEquals(1, result.total());
        assertEquals(Objects.requireNonNull(human.tryGetId()), result.data().getFirst());

    }

    private Id nextRootId() {
        return PhysicalId.of(nextTreeId++, 0);
    }

}