package org.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.mocks.EntityFoo;
import org.metavm.object.type.Klass;
import org.metavm.util.BootstrapUtils;
import org.metavm.util.TestConstants;
import org.metavm.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityContextTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(EntityContextTest.class);

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

    public void testContainsUniqueKey() {
        try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            Assert.assertNotNull(context.getParent());
            Assert.assertTrue(context.getParent().containsUniqueKey(Klass.UNIQUE_CODE, Klass.class.getName()));
        }
    }

}