package org.manul.entity.natives;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.manul.entity.EntityContextFactory;
import org.manul.entity.StdKlass;
import org.manul.flow.MethodBuilder;
import org.manul.flow.Nodes;
import org.manul.object.instance.IndexKeyRT;
import org.manul.object.instance.core.ClassInstance;
import org.manul.object.instance.core.IInstanceContext;
import org.manul.object.instance.core.StringInstance;
import org.manul.object.instance.core.StringReference;
import org.manul.object.type.FieldBuilder;
import org.manul.object.type.Index;
import org.manul.object.type.Types;
import org.manul.util.*;

import java.util.List;
import java.util.Map;

import static org.manul.util.Instances.stringInstance;

@Slf4j
public class StringTest extends TestCase {

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
                var fooKlass = TestUtils.newKlassBuilder("Foo").build();
                var nameField = FieldBuilder.newBuilder("name", fooKlass, Types.getStringType()).build();
                var getNameMethod = MethodBuilder.newBuilder(fooKlass, "getName")
                        .returnType(Types.getStringType())
                        .build();
                {
                    var code = getNameMethod.getCode();
                    Nodes.load(0, fooKlass.getType(), code);
                    Nodes.getField(nameField.getRef(), code);
                    Nodes.ret(code);
                    code.emitCode();
                }
                new Index(fooKlass.nextChildId(), fooKlass, "nameIdx", null, true, Types.getStringType(), getNameMethod);
                context.bind(fooKlass);
                context.finish();
                return fooKlass.getId();
            }
        });
        var fooId = TestUtils.doInTransaction(() -> {
            try (var context = newContext()) {
                context.loadKlasses();
                var fooKlass = context.getKlass(fooKlassId);
                var sField = fooKlass.getFieldByName("name");
                var s = stringInstance("Metavm");
                var foo = ClassInstance.create(
                        context.allocateRootId(),
                        Map.of(sField, s),
                        fooKlass.getType()
                );
                context.bind(foo);
                context.finish();
                return foo.getId();
            }
        });
        try (var context = newContext()) {
            context.loadKlasses();
            var fooKlass = context.getKlass(fooKlassId);
            var nameIndex = fooKlass.getIndices().getFirst();
            var key = new IndexKeyRT(nameIndex, List.of(stringInstance("Metavm")));
            var r = context.selectFirstByKey(key);
            Assert.assertNotNull(r);
            var foo = r.resolveObject();
            var s = foo.getField("name");
            Assert.assertTrue(s instanceof StringReference);
            Assert.assertEquals("Metavm", Instances.toJavaString(s));
        }
    }

    public void testToIndexBytes() {
        try (var context = newContext()) {
            var s = stringInstance("Metavm");
            var bytes = BytesUtils.toIndexBytes(s);
            log.trace("Index bytes length: {}", bytes.length);
        }
    }

    public void testStringCompare() {
        Assert.assertTrue(StringInstance.equals(stringInstance("Metavm"), stringInstance("Metavm")));
        Assert.assertFalse(StringInstance.equals(stringInstance("Metavm"), stringInstance("VM")));
        Assert.assertEquals(0, StringInstance.compare(stringInstance("Metavm"), stringInstance("Metavm")));
        Assert.assertTrue(StringInstance.compare(stringInstance("Metavm"), stringInstance("VM")) < 0);
        Assert.assertTrue(StringInstance.compare(stringInstance("Metavm"), stringInstance("Meta")) > 0);
    }

    private IInstanceContext newContext() {
        return entityContextFactory.newContext(TestConstants.APP_ID);
    }

}