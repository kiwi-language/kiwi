package org.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.beans.BeanDefinitionRegistry;
import org.metavm.object.instance.core.ClassInstanceBuilder;
import org.metavm.object.type.Klass;
import org.metavm.util.*;

import java.util.Objects;

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
        Assert.assertTrue(metaContext.containsEntity(Klass.class, fooKlassId));
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var context = entityContextFactory.newContext(TestConstants.APP_ID, metaContext)) {
                var parent = Objects.requireNonNull(context.getParent());
                Assert.assertTrue(parent.containsUniqueKey(Klass.UNIQUE_QUALIFIED_NAME, "Foo"));
                var instCtx = context.getInstanceContext();
                var fooKlass = context.getKlass(fooKlassId);
                var foo = ClassInstanceBuilder.newBuilder(fooKlass.getType()).build();
                instCtx.bind(foo);
                var reg = BeanDefinitionRegistry.getInstance(context);
                reg.getInterceptors();
                context.finish();
            }
        });
    }

}