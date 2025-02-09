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
import org.metavm.object.type.Types;
import org.metavm.util.BootstrapUtils;
import org.metavm.util.Instances;
import org.metavm.util.TestConstants;
import org.metavm.util.TestUtils;

import java.util.ArrayList;
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
                var fooClass = TestUtils.newKlassBuilder("Foo").build();
                FieldBuilder.newBuilder("name", fooClass, Types.getStringType()).build();
                context.bind(fooClass);
                context.finish();
                return fooClass.getId();
            }
        });
        var listId = TestUtils.doInTransaction(() -> {
            try (var context = newContext()) {
                context.loadKlasses();
                var fooKlass = context.getKlass(fooKlassId);
                var nameField = fooKlass.getFieldByName("name");
                var size = 3;
                var values = new ArrayList<Value>();
                for (int i = 0; i < size; i++) {
                    var foo = ClassInstance.create(
                            Map.of(
                                    nameField, Instances.stringInstance(String.format("foo%d", i))
                            ),
                            fooKlass.getType()
                    );
                    values.add(foo.getReference());
                }
                var list = Instances.newList(StdKlass.childList.type(), values);
                context.bind(list);
                context.finish();
                return list.getId();
            }
        });
        try (var context = newContext()) {
            context.loadKlasses();
            var inst = (ClassInstance) context.get(listId);
            var list = Instances.toJavaList(inst);
            Assert.assertEquals(3, list.size());
            for (int i = 0; i < 3; i++) {
                var foo = list.get(i).resolveMvObject();
                var name = Instances.toJavaString(foo.getField("name"));
                Assert.assertEquals(String.format("foo%d", i), name);
            }
        }
    }

    private IInstanceContext newContext() {
        return entityContextFactory.newContext(TestConstants.APP_ID);
    }

}