package tech.metavm.object.instance.search;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.expression.Expressions;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.util.*;

import java.util.Set;

public class SearchBuilderTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchBuilderTest.class);

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void test() {
        var fooTypes = MockUtils.createFooTypes(true);
        var foo = MockUtils.createFoo(fooTypes, true);
        var baz = foo.getClassInstance(fooTypes.fooQuxField());

        var condition = Expressions.and(
                Expressions.fieldStartsWith(
                        fooTypes.fooNameField(),
                        foo.getStringField(fooTypes.fooNameField())
                ),
                Expressions.fieldEq(fooTypes.fooQuxField(), baz)
        );
        var query = new SearchQuery(
                Constants.ROOT_APP_ID,
                Set.of(100L),
                condition,
                false,
                1,
                20,
                0
        );

        var queryString = SearchBuilder.buildQueryString(query);
        LOGGER.info(queryString);
    }

}