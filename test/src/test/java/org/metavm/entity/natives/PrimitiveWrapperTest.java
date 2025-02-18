package org.metavm.entity.natives;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.type.FieldBuilder;
import org.metavm.object.type.Types;
import org.metavm.util.BootstrapUtils;
import org.metavm.util.Instances;
import org.metavm.util.TestConstants;
import org.metavm.util.TestUtils;

import java.util.List;
import java.util.Map;

public class PrimitiveWrapperTest extends TestCase {

    private EntityContextFactory entityContextFactory;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        entityContextFactory = bootResult.entityContextFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        entityContextFactory = null;
    }

    public void test() {
        var fooKlassId = TestUtils.doInTransaction(() -> {
            try (var context = newContext()) {
                var fooKlas = TestUtils.newKlassBuilder("Foo").build();
                FieldBuilder.newBuilder("elements", fooKlas, Types.getArrayType(Types.getNullableAnyType()))
                        .build();
                context.bind(fooKlas);
                context.finish();
                return fooKlas.getId();
            }
        });
        var fooId = TestUtils.doInTransaction(() -> {
            try(var context = newContext()) {
                context.loadKlasses();
                var fooKlass = context.getKlass(fooKlassId);
                var elementsField = fooKlass.getFieldByName("elements");
                var foo = ClassInstance.create(
                        context.allocateRootId(),
                        Map.of(
                                elementsField,
                                new ArrayInstance(
                                        Types.getArrayType(Types.getNullableAnyType()),
                                        List.of(
                                                Instances.wrappedIntInstance(1),
                                                Instances.wrappedIntInstance(2),
                                                Instances.wrappedIntInstance(3)
                                        )
                                ).getReference()
                        ),
                        fooKlass.getType()
                );
                context.bind(foo);
                context.finish();
                return foo.getId();
            }
        });
        try(var context = newContext()) {
            context.loadKlasses();
            var foo = (ClassInstance) context.get(fooId);
            var elements = foo.getField("elements").resolveArray();
            Assert.assertEquals(3, elements.size());
            var e = elements.get(0).resolveObject();
            Assert.assertSame(StdKlass.integer.get(), e.getInstanceKlass());
        }
    }

    private IInstanceContext newContext() {
        return entityContextFactory.newContext(TestConstants.APP_ID);
    }

}
