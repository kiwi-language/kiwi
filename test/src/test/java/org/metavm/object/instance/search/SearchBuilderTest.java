package org.metavm.object.instance.search;

import junit.framework.TestCase;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.expression.Expressions;
import org.metavm.util.Constants;
import org.metavm.util.MockUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class SearchBuilderTest extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(SearchBuilderTest.class);

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
                Expressions.fieldEq(fooTypes.fooQuxField(), baz.getReference())
        );
        var query = new SearchQuery(
                Constants.ROOT_APP_ID,
                Set.of("1"),
                condition,
                false,
                1,
                20,
                0
        );

        var queryString = SearchBuilder.buildQueryString(query);
        logger.info(queryString);
    }

}