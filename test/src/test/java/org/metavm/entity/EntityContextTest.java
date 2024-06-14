package org.metavm.entity;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.metavm.entity.mocks.EntityFoo;
import org.metavm.util.BootstrapUtils;
import org.metavm.util.TestConstants;
import org.metavm.util.TestUtils;

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
        TestUtils.doInTransactionWithoutResult(() -> {
            var foo = new EntityFoo("foo");
            try (var entityContext = entityContextFactory.newContext(TestConstants.APP_ID)) {
                entityContext.bind(foo);
                entityContext.finish();
            }
        });
    }


}