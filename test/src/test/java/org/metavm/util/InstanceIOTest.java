package org.metavm.util;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.function.Function;

public class InstanceIOTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(InstanceIOTest.class);

    @Override
    protected void setUp() throws Exception {
        TestUtils.ensureStringKlassInitialized();
    }

    public void testWriteString() {
        String s = "hello world";
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        output.writeUTF(s);
        var input = InstanceInput.create(bout.toByteArray(), null);
        Assert.assertEquals(s, input.readUTF());
    }

    public void testWriteInt() {
        int i = new Random().nextInt();
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        output.writeInt(i);
        var input = InstanceInput.create(bout.toByteArray(), null);
        Assert.assertEquals(i, input.readInt());
    }

    public void testWriteDouble() {
        var ran = new Random();
        for (int i = 0; i < 1000; i++) {
            double d = ran.nextDouble(Double.MAX_VALUE);
            var bout = new ByteArrayOutputStream();
            var output = new InstanceOutput(bout);
            output.writeDouble(d);
            byte[] bytes = bout.toByteArray();
            var input = InstanceInput.create(bytes, null);
            Assert.assertEquals(d, input.readDouble(), 0.0);
        }
    }

    public void testWriteLong() {
        var ran = new Random();
        for (int i = 0; i < 1000; i++) {
            long id = ran.nextLong();
            var bout = new ByteArrayOutputStream();
            var output = new InstanceOutput(bout);
            output.writeLong(id);
            byte[] bytes = bout.toByteArray();
            var input = InstanceInput.create(bytes, null);
            Assert.assertEquals(id, input.readLong());
        }
    }

    private static final String BYTE_FILE = "/Users/leen/workspace/object/src/test/resources/bytes/1240104967";

    public void testArrayChildField() {
        var fooKlass = TestUtils.newKlassBuilder("Foo").build();
        var stringArrayType = new ArrayType(Types.getStringType(), ArrayKind.DEFAULT);
        var namesField = FieldBuilder.newBuilder("names", fooKlass, stringArrayType).build();
        var entityMap = new HashMap<Id, Instance>();
        fooKlass.forEachDescendant(i -> entityMap.put(i.getId(), i));
        var names = new ArrayInstance(stringArrayType, List.of(Instances.stringInstance("foo")));
        Assert.assertTrue(names.getReference().isValueReference());
        var foo = ClassInstanceBuilder.newBuilder(fooKlass.getType())
                .data(Map.of(namesField, names.getReference()))
                .id(PhysicalId.of(1L, 0L))
                .build();
        var input = new InstanceInput(new ByteArrayInputStream(InstanceOutput.toBytes(foo)), entityMap::get,
                i -> {}, id -> null);
        var recovered = (ClassInstance) input.readSingleMessageGrove();
        var recoveredNames = recovered.getField(namesField).resolveArray();
        var name = recoveredNames.getFirst();
        Assert.assertEquals("foo", Instances.toJavaString(name));
    }

    public void testInheritance() {
        var baseKlass = TestUtils.newKlassBuilder("Base").build();
        var nameField = FieldBuilder.newBuilder("name", baseKlass, Types.getStringType()).build();
        var derivedKlass = TestUtils.newKlassBuilder("Derived").superType(baseKlass.getType()).build();
        var codeField = FieldBuilder.newBuilder("code", derivedKlass, Types.getLongType()).build();
        var entityMap = new HashMap<Id, Instance>();
        baseKlass.forEachDescendant(i -> entityMap.put(i.getId(), i));
        derivedKlass.forEachDescendant(i -> entityMap.put(i.getId(), i));

        var inst = ClassInstanceBuilder.newBuilder(derivedKlass.getType())
                .data(Map.of(
                        nameField, Instances.stringInstance("foo"),
                        codeField, Instances.longInstance(1)
                ))
                .build();
        inst.initId(PhysicalId.of(1L, 1L));
        var input = new InstanceInput(new ByteArrayInputStream(InstanceOutput.toBytes(inst)), entityMap::get, i -> {
        }, id -> null);
        var recovered = (ClassInstance) input.readSingleMessageGrove();
        Assert.assertEquals(Instances.toJavaString(inst.getField(nameField)),
                Instances.toJavaString(recovered.getField(nameField)));
    }

    public void test() {
        String fooName = "foo", barCode = "bar001";
        Klass fooKlass = TestUtils.newKlassBuilder("Foo", "Foo").build();
        Klass barKlass = TestUtils.newKlassBuilder("Bar", "Bar").build();
        Klass quxKlass = TestUtils.newKlassBuilder("Qux", "Qux").build();

        Field nameField = FieldBuilder
                .newBuilder("name", fooKlass, Types.getStringType()).build();
        Field barField = FieldBuilder
                .newBuilder("bar", fooKlass, barKlass.getType()).build();
        Field quxField = FieldBuilder.newBuilder("qux", fooKlass, quxKlass.getType()).build();

        Field barCodeField = FieldBuilder
                .newBuilder("code", barKlass, Types.getStringType()).build();

        Field quxNameField = FieldBuilder
                .newBuilder("name", quxKlass, Types.getStringType()).build();

        var entityMap = new HashMap<Id, Instance>();
        fooKlass.forEachDescendant(i -> entityMap.put(i.getId(), i));
        barKlass.forEachDescendant(i -> entityMap.put(i.getId(), i));
        quxKlass.forEachDescendant(i -> entityMap.put(i.getId(), i));

        var barInst = new MvClassInstance(
                PhysicalId.of(30001L, 1L),
                Map.of(
                        barCodeField,
                        Instances.stringInstance(barCode)
                ),
                barKlass
        );

        var quxInst = new MvClassInstance(
                PhysicalId.of(30002L, 0L),
                Map.of(
                        quxNameField,
                        Instances.stringInstance("qux001")
                ),
                quxKlass
        );

        var fooInst = new MvClassInstance(
                PhysicalId.of(30001L, 0L),
                Map.of(
                        nameField, Instances.stringInstance(fooName),
                        barField, barInst.getReference(),
                        quxField, quxInst.getReference()
                ),
                fooKlass
        );
        barInst.setParentInternal(fooInst, true);

        Function<Id, Instance> resolveInst = id -> {
            var entity = entityMap.get(id);
            if (entity != null) return entity;
            if(Objects.equals(id, fooInst.tryGetId()))
                return ClassInstance.allocateUninitialized(id);
            else if(Objects.equals(id, barInst.tryGetId()))
                return ClassInstance.allocateUninitialized(id);
            else if(Objects.equals(id, quxInst.tryGetId()))
                return quxInst;
            else
                throw new InternalException(String.format("Invalid id %s", id));
        };
        var bytes = InstanceOutput.toBytes(fooInst);
        var input = new InstanceInput(new ByteArrayInputStream(bytes), resolveInst, i -> {}, id -> null);
        var recoveredFooInst = (ClassInstance) input.readSingleMessageGrove();
        MatcherAssert.assertThat(recoveredFooInst, InstanceMatcher.of(fooInst));
        new StreamVisitor(new ByteArrayInputStream(bytes)) {
        }.visitGrove();
    }

    public void testChar() {
        char c1 = 'a', c2 = '码';
        var bout = new ByteArrayOutputStream();
        var out = new InstanceOutput(bout);
        out.writeChar(c1);
        out.writeChar(c2);
        var in = new InstanceInput(new ByteArrayInputStream(bout.toByteArray()));
        Assert.assertEquals(c1, in.readChar());
        Assert.assertEquals(c2, in.readChar());
    }

}