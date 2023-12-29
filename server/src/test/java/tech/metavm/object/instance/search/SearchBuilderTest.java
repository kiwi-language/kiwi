package tech.metavm.object.instance.search;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.expression.Expression;
import tech.metavm.expression.Expressions;
import tech.metavm.object.type.Field;
import tech.metavm.util.Constants;
import tech.metavm.util.Instances;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;

import java.util.Set;

public class SearchBuilderTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchBuilderTest.class);

    @Override
    protected void setUp() throws Exception {
        MockRegistry.setUp(new MockIdProvider());
    }

    public void test() {
        Field fooQuxField = MockRegistry.getField(Foo.class, "qux");
        ClassInstance foo = MockRegistry.getFooInstance();
        ClassInstance qux = foo.getClassInstance(fooQuxField);

        Expression condition = Expressions.and(
                Expressions.fieldStartsWith(
                        MockRegistry.getField(Foo.class, "name"),
                        Instances.stringInstance("Big")
                ),
                Expressions.fieldEq(fooQuxField, qux)
        );

        SearchQuery query = new SearchQuery(
                Constants.ROOT_APP_ID,
                Set.of(100L),
                condition,
                false,
                1,
                20,
                0
        );

        String queryString = SearchBuilder.buildQueryString(query);
        LOGGER.info(queryString);
    }

}