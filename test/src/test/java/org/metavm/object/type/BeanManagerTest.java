package org.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.beans.BeanDefinitionRegistry;
import org.metavm.entity.AttributeNames;
import org.metavm.entity.BeanKinds;
import org.metavm.entity.EntityContextFactory;
import org.metavm.flow.MaxesComputer;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Nodes;
import org.metavm.flow.Parameter;
import org.metavm.util.BootstrapUtils;
import org.metavm.util.TestConstants;
import org.metavm.util.TestUtils;

import java.util.List;

public class BeanManagerTest extends TestCase {

    private BeanManager beanManager;
    private EntityContextFactory entityContextFactory;

    @Override
    protected void setUp() throws Exception {
        beanManager = new BeanManager();
        var bootResult = BootstrapUtils.bootstrap();
        entityContextFactory = bootResult.entityContextFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        entityContextFactory = null;
    }

    public void test() {
        try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var fooServiceKlass = TestUtils.newKlassBuilder("FooService", "FooService")
                    .addAttribute(AttributeNames.BEAN_NAME, "fooService")
                    .addAttribute(AttributeNames.BEAN_KIND, BeanKinds.COMPONENT)
                    .build();
            addSimpleConstructor(fooServiceKlass);
            var configKlass = TestUtils.newKlassBuilder("Config", "Config")
                    .addAttribute(AttributeNames.BEAN_NAME, "config")
                    .addAttribute(AttributeNames.BEAN_KIND, BeanKinds.CONFIGURATION)
                    .build();
            addSimpleConstructor(configKlass);

            var barServiceKlass = TestUtils.newKlassBuilder("BarService", "BarService")
                    .build();
            var field = FieldBuilder.newBuilder("fooService", "fooService", barServiceKlass, fooServiceKlass.getType())
                    .build();
            var constructor = MethodBuilder.newBuilder(barServiceKlass, "BarService", "BarService")
                    .isConstructor(true)
                    .parameters(new Parameter(null, "fooService", "fooService", fooServiceKlass.getType()))
                    .returnType(barServiceKlass.getType())
                    .build();
            {
                var scope = constructor.getScope();
                Nodes.this_(scope);
                Nodes.dup(scope);
                Nodes.argument(constructor, 0);
                Nodes.setField(field, scope);
                Nodes.ret(scope);
            }
            var factoryMethod = MethodBuilder.newBuilder(configKlass, "barService", "barService")
                    .addAttribute(AttributeNames.BEAN_NAME, "barService")
                    .parameters(new Parameter(null, "fooService", "fooService", fooServiceKlass.getType()))
                    .returnType(barServiceKlass.getType())
                    .build();
            {
                Nodes.argument(factoryMethod, 0);
                Nodes.newObject(
                        factoryMethod.getScope(),
                        constructor,
                        false,
                        false
                );
                Nodes.ret(factoryMethod.getScope());
            }
            var registry = BeanDefinitionRegistry.getInstance(context);
            configKlass.accept(new MaxesComputer());
            fooServiceKlass.accept(new MaxesComputer());
            barServiceKlass.accept(new MaxesComputer());
            beanManager.createBeans(List.of(configKlass, fooServiceKlass), registry, context);
            var fooService = registry.getBean("fooService");
            Assert.assertTrue(fooServiceKlass.getType().isInstance(fooService.getReference()));
            var barService = registry.getBean("barService");
            Assert.assertTrue(barServiceKlass.getType().isInstance(barService.getReference()));
            Assert.assertSame(fooService, barService.getField(field).resolveObject());
            Assert.assertEquals(1, registry.getBeansOfType(fooServiceKlass.getType()).size());
            Assert.assertEquals(1, registry.getBeansOfType(barServiceKlass.getType()).size());
        }

    }

    private void addSimpleConstructor(Klass klass) {
        var constructor = MethodBuilder.newBuilder(klass, "FooService", "FooService")
                .isConstructor(true)
                .returnType(klass.getType())
                .build();
        var scope = constructor.getScope();
        Nodes.this_(scope);
        Nodes.ret(scope);
    }

}