package org.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.beans.BeanDefinitionRegistry;
import org.metavm.entity.AttributeNames;
import org.metavm.entity.BeanKinds;
import org.metavm.entity.EntityContextFactory;
import org.metavm.flow.*;
import org.metavm.util.BootstrapUtils;
import org.metavm.util.TestConstants;

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
            var fooServiceKlass = KlassBuilder.newBuilder("FooService", "FooService")
                    .addAttribute(AttributeNames.BEAN_NAME, "fooService")
                    .addAttribute(AttributeNames.BEAN_KIND, BeanKinds.COMPONENT)
                    .build();
            addSimpleConstructor(fooServiceKlass);
            var configKlass = KlassBuilder.newBuilder("Config", "Config")
                    .addAttribute(AttributeNames.BEAN_NAME, "config")
                    .addAttribute(AttributeNames.BEAN_KIND, BeanKinds.CONFIGURATION)
                    .build();
            addSimpleConstructor(configKlass);

            var barServiceKlass = KlassBuilder.newBuilder("BarService", "BarService")
                    .build();
            var field = FieldBuilder.newBuilder("fooService", "fooService", barServiceKlass, fooServiceKlass.getType())
                    .build();
            var constructor = MethodBuilder.newBuilder(barServiceKlass, "BarService", "BarService")
                    .isConstructor(true)
                    .parameters(new Parameter(null, "fooService", "fooService", fooServiceKlass.getType()))
                    .returnType(barServiceKlass.getType())
                    .build();
            {
                var self = Nodes.self("self", barServiceKlass, constructor.getRootScope());
                var input = Nodes.input(constructor);
                Nodes.updateField("setFooService", Values.node(self),
                        field, Values.nodeProperty(input, input.getKlass().getFieldByName("fooService")),
                        constructor.getRootScope()
                );
                Nodes.ret("return", constructor.getRootScope(), Values.node(self));
            }
            var factoryMethod = MethodBuilder.newBuilder(configKlass, "barService", "barService")
                    .addAttribute(AttributeNames.BEAN_NAME, "barService")
                    .parameters(new Parameter(null, "fooService", "fooService", fooServiceKlass.getType()))
                    .returnType(barServiceKlass.getType())
                    .build();
            {
                var input = Nodes.input(factoryMethod);
                var barService = Nodes.newObject(
                        "barService",
                        factoryMethod.getRootScope(),
                        constructor,
                        List.of(
                                new Argument(
                                        null,
                                        constructor.getParameter(0).getRef(),
                                        Values.nodeProperty(
                                                input,
                                                input.getKlass().getFieldByName("fooService")
                                        )
                                )
                        ),
                        false,
                        false
                );
                Nodes.ret("return", factoryMethod.getRootScope(), Values.node(barService));
            }
            var registry = BeanDefinitionRegistry.getInstance(context);
            beanManager.createBeans(List.of(configKlass, fooServiceKlass), registry, context);
            var fooService = registry.getBean("fooService");
            Assert.assertTrue(fooServiceKlass.getType().isInstance(fooService));
            var barService = registry.getBean("barService");
            Assert.assertTrue(barServiceKlass.getType().isInstance(barService));
            Assert.assertSame(fooService, barService.getField(field));
            Assert.assertEquals(1, registry.getBeansOfType(fooServiceKlass.getType()).size());
            Assert.assertEquals(1, registry.getBeansOfType(barServiceKlass.getType()).size());
        }

    }

    private void addSimpleConstructor(Klass klass) {
        var constructor = MethodBuilder.newBuilder(klass, "FooService", "FooService")
                .isConstructor(true)
                .returnType(klass.getType())
                .build();
        var self = Nodes.self("self", klass, constructor.getRootScope());
        Nodes.ret("return", constructor.getRootScope(), Values.node(self));
    }

}