package org.manul.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.manul.beans.BeanDefinitionRegistry;
import org.manul.object.instance.core.ClassInstanceBuilder;
import org.manul.object.type.Klass;
import org.manul.util.BootstrapUtils;
import org.manul.util.Instances;
import org.manul.util.TestConstants;
import org.manul.util.TestUtils;

public class MetaContextCacheTest extends TestCase {

    private MetaContextCache cache;
    private EntityContextFactory entityContextFactory;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        entityContextFactory = bootResult.entityContextFactory();
        cache = bootResult.metaContextCache();
    }

    @Override
    protected void tearDown() throws Exception {
        cache = null;
        entityContextFactory = null;
    }

    public void test() {
        var fooKlassId = TestUtils.doInTransaction(() -> {
            try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
                var fooKlass = TestUtils.newKlassBuilder("Foo").build();
                context.bind(fooKlass);
                context.finish();
                return fooKlass.getId();
            }
        });
        var metaContext = cache.get(TestConstants.APP_ID);
        Assert.assertTrue(metaContext.contains(fooKlassId));
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var context = entityContextFactory.newContext(TestConstants.APP_ID, metaContext)) {
                Assert.assertTrue(context.containsUniqueKey(Klass.UNIQUE_QUALIFIED_NAME,
                        Instances.stringInstance("Foo")));
                var fooKlass = context.getKlass(fooKlassId);
                var foo = ClassInstanceBuilder.newBuilder(fooKlass.getType(), context.allocateRootId()).build();
                context.bind(foo);
                var reg = BeanDefinitionRegistry.getInstance(context);
                reg.getInterceptors();
                context.finish();
            }
        });
    }

}