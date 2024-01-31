package tech.metavm.entity;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.mocks.EntityFoo;
import tech.metavm.util.BootstrapUtils;
import tech.metavm.util.TestConstants;
import tech.metavm.util.TestUtils;

public class EntityContextTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(EntityContextTest.class);

    private EntityContextFactory entityContextFactory;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        entityContextFactory = bootResult.entityContextFactory();
    }

    @Override
    protected void tearDown() {
        entityContextFactory = null;
    }

    public void test() {
        TestUtils.beginTransaction();
        var foo = new EntityFoo("foo");
        try (var entityContext = entityContextFactory.newContext(TestConstants.APP_ID)) {
            entityContext.bind(foo);
            entityContext.finish();
        }
        TestUtils.commitTransaction();
    }


}