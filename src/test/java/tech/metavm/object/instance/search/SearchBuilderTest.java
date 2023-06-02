package tech.metavm.object.instance.search;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.expression.Expression;
import tech.metavm.expression.ExpressionUtil;
import tech.metavm.object.meta.Field;
import tech.metavm.util.InstanceUtils;
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

        Expression condition = ExpressionUtil.and(
                ExpressionUtil.fieldStartsWith(
                        MockRegistry.getField(Foo.class, "name"),
                        InstanceUtils.stringInstance("Big")
                ),
                ExpressionUtil.fieldEq(fooQuxField, qux)
        );

        SearchQuery query = new SearchQuery(
                -1L,
                Set.of(100L),
                condition,
                1,
                20
        );

        String queryString = SearchBuilder.buildQueryString(query);
        LOGGER.info(queryString);
    }

}