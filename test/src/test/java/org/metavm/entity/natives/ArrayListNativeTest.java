package org.metavm.entity.natives;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.FieldBuilder;
import org.metavm.object.type.KlassType;
import org.metavm.object.type.Types;
import org.metavm.util.BootstrapUtils;
import org.metavm.util.Instances;
import org.metavm.util.TestConstants;
import org.metavm.util.TestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ArrayListNativeTest extends TestCase {

    private EntityContextFactory entityContextFactory;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        entityContextFactory = bootResult.entityContextFactory();
    }

    public void test() {
        var fooKlassId = TestUtils.doInTransaction(() -> {
            try (var context = newContext()) {
                var barKlass =TestUtils.newKlassBuilder("Bar").build();
                FieldBuilder.newBuilder("code", barKlass, Types.getStringType()).build();

                var fooKlass = TestUtils.newKlassBuilder("Foo").build();
                var barListType = KlassType.create(StdKlass.list.get(), List.of(barKlass.getType()));
                FieldBuilder.newBuilder("bars", fooKlass, barListType).build();
                context.bind(fooKlass);
                context.finish();
                return fooKlass.getId();
            }
        });
        var listId = TestUtils.doInTransaction(() -> {
            try (var context = newContext()) {
                context.loadKlasses();
                var fooKlass = context.getKlass(fooKlassId);
                var fooBarsField = fooKlass.getFieldByName("bars");
                var barKlass = context.getKlassByQualifiedName("Bar");
                var barCodeField = barKlass.getFieldByName("code");

                var size = 3;
                var values = new ArrayList<Value>();
                for (int i = 0; i < size; i++) {
                    var bar = ClassInstance.create(
                            context.allocateRootId(),
                            Map.of(
                                    barCodeField, Instances.stringInstance(String.format("bar%d", i))
                            ),
                            barKlass.getType()
                    );
                    values.add(bar.getReference());
                }
                var barArrayListType = KlassType.create(StdKlass.arrayList.get(), List.of(barKlass.getType()));
                var list = Instances.newList(barArrayListType, values);
                var foo = ClassInstance.create(context.allocateRootId(), Map.of(
                        fooBarsField, list.getReference()
                ), fooKlass.getType());
                context.bind(foo);
                context.finish();
                return foo.getId();
            }
        });
        try (var context = newContext()) {
            context.loadKlasses();
            var foo = (ClassInstance) context.get(listId);
            var inst = foo.getField("bars").resolveObject();
            var list = Instances.toJavaList(inst);
            Assert.assertEquals(3, list.size());
            for (int i = 0; i < 3; i++) {
                var bar = list.get(i).resolveMvObject();
                var name = Instances.toJavaString(bar.getField("code"));
                Assert.assertEquals(String.format("bar%d", i), name);
            }
        }
    }

    private IInstanceContext newContext() {
        return entityContextFactory.newContext(TestConstants.APP_ID);
    }

}