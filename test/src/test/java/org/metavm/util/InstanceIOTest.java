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

    public void testWriteString() {
        String s = "hello world";
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        output.writeString(s);
        var input = InstanceInput.create(bout.toByteArray(), null);
        Assert.assertEquals(s, input.readString());
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

//    public void testLargeInstance() {
//        var classType = MockRegistry.getDefContext().getClassType(ClassType.class);
//        var classTypeInst = MockRegistry.getDefContext().getInstance(classType);
//        var bytes = InstanceOutput.toByteArray(classTypeInst, true);
//        System.out.println(bytes.length);
//
//        ByteArrayOutputStream bout = new ByteArrayOutputStream();
//        new StreamCopier(new ByteArrayInputStream(bytes), bout).visit();
//        Assert.assertTrue(Arrays.equals(bytes, bout.toByteArray()));
//
//        var deserialized = InstanceInput.readFromBytes(bytes, MockRegistry.getInstanceContext());
//        MatcherAssert.assertThat(deserialized, InstanceMatcher.of(classTypeInst));
//
//        var instanceDTO = classTypeInst.toDTO();
//        String json = NncUtils.toJSONString(instanceDTO);
//        System.out.println(json.getBytes(StandardCharsets.UTF_8).length);
//    }

    private static final String BYTE_FILE = "/Users/leen/workspace/object/src/test/resources/bytes/1240104967";

//    public void testPerf() {
//        var bytes = TestUtils.readBytes(BYTE_FILE);
//        int runs = 300;
//        long start = System.currentTimeMillis();
//        for (int i = 0; i < runs; i++) {
//            var visitor = new StreamVisitor(new ByteArrayInputStream(bytes));
//            visitor.visit();
//        }
//        long elapsed = System.currentTimeMillis() - start;
//        System.out.printf("average time: %d%n", elapsed / runs);
//    }

    public void testArrayChildField() {
        var fooKlass = TestUtils.newKlassBuilder("Foo").build();
        var stringArrayType = new ArrayType(Types.getStringType(), ArrayKind.READ_WRITE);
        var namesField = FieldBuilder.newBuilder("names", "names", fooKlass, stringArrayType)
                .isChild(true)
                .build();
        TestUtils.initEntityIds(fooKlass);
        var names = new ArrayInstance(stringArrayType, List.of(Instances.stringInstance("foo")));
        names.initId(PhysicalId.of(1L, 1L, stringArrayType));
        var foo = ClassInstanceBuilder.newBuilder(fooKlass.getType())
                .data(Map.of(namesField, names.getReference()))
                .id(PhysicalId.of(1L, 0L, fooKlass.getType()))
                .build();
        var instanceMap = new HashMap<Id, DurableInstance>();
        var input = new InstanceInput(new ByteArrayInputStream(InstanceOutput.toBytes(foo)), id -> null,
                i -> instanceMap.put(i.getId(), i), id -> id.equals(fooKlass.getId()) ? fooKlass : null, id -> null);
        var recovered = (ClassInstance) input.readSingleMessageGrove();
        var recoveredNames = recovered.getField(namesField).resolveArray();
        var name = recoveredNames.get(0);
        Assert.assertEquals(Instances.stringInstance("foo"), name);
        Assert.assertNotNull(instanceMap.get(names.getId()));
    }

    public void testInheritance() {
        var baseKlass = TestUtils.newKlassBuilder("Base").build();
        var nameField = FieldBuilder.newBuilder("name", "name", baseKlass, Types.getStringType()).build();
        var derivedKlass = TestUtils.newKlassBuilder("Derived").superType(baseKlass.getType()).build();
        var codeField = FieldBuilder.newBuilder("code", "code", derivedKlass, Types.getLongType()).build();
        TestUtils.initEntityIds(derivedKlass);
        var inst = ClassInstanceBuilder.newBuilder(derivedKlass.getType())
                .data(Map.of(
                        nameField, Instances.stringInstance("foo"),
                        codeField, Instances.longInstance(1)
                ))
                .build();
        inst.initId(PhysicalId.of(1L, 1L, inst.getType()));
        var input = new InstanceInput(new ByteArrayInputStream(InstanceOutput.toBytes(inst)), id -> null, i -> {
        }, id -> id.equals(derivedKlass.getId()) ? derivedKlass : null, id -> null);
        var recovered = (ClassInstance) input.readSingleMessageGrove();
        Assert.assertEquals(inst.getField(nameField), recovered.getField(nameField));
    }

    public void test() {
        String fooName = "foo", barCode = "bar001";
        Klass fooType = TestUtils.newKlassBuilder("Foo", "Foo").build();
        Klass barType = TestUtils.newKlassBuilder("Bar", "Bar").build();
        Klass quxType = TestUtils.newKlassBuilder("Qux", "Qux").build();

        fooType.initId(PhysicalId.of(10001L, 0L, TestUtils.mockClassType()));
        barType.initId(PhysicalId.of(10002L, 0L, TestUtils.mockClassType()));
        quxType.initId(PhysicalId.of(10003L, 0L, TestUtils.mockClassType()));

        Field nameField = FieldBuilder
                .newBuilder("name", "name", fooType, Types.getStringType()).build();
        nameField.initId(PhysicalId.of(10001L, 1L, TestUtils.mockClassType()));
        Field barField = FieldBuilder
                .newBuilder("bar", "bar", fooType, barType.getType()).isChild(true).build();
        barField.initId(PhysicalId.of(10001L, 2L, TestUtils.mockClassType()));
        Field quxField = FieldBuilder.newBuilder("qux", "qux", fooType, quxType.getType()).build();
        quxField.initId(PhysicalId.of(10001L, 3L, TestUtils.mockClassType()));

        Field barCodeField = FieldBuilder
                .newBuilder("code", "code", barType, Types.getStringType()).build();
        barCodeField.initId(PhysicalId.of(10002L, 1L, TestUtils.mockClassType()));

        Field quxNameField = FieldBuilder
                .newBuilder("name", "name", quxType, Types.getStringType()).build();
        quxNameField.initId(PhysicalId.of(10003L, 1L, TestUtils.mockClassType()));

        var barInst = new ClassInstance(
                PhysicalId.of(30001L, 1L, TestUtils.mockClassType()),
                Map.of(
                        barCodeField,
                        new StringInstance(barCode, Types.getStringType())
                ),
                barType
        );

        var quxInst = new ClassInstance(
                PhysicalId.of(30002L, 0L, TestUtils.mockClassType()),
                Map.of(
                        quxNameField,
                        new StringInstance("qux001", Types.getStringType())
                ),
                quxType
        );

        var fooInst = new ClassInstance(
                PhysicalId.of(30001L, 0L, TestUtils.mockClassType()),
                Map.of(
                        nameField, new StringInstance(fooName, Types.getStringType()),
                        barField, barInst.getReference(),
                        quxField, quxInst.getReference()
                ),
                fooType
        );
        barInst.setParentInternal(fooInst, barField, true);

        Function<Id, DurableInstance> resolveInst = id -> {
            if(Objects.equals(id, fooInst.tryGetId()))
                return ClassInstance.allocateUninitialized(id);
            else if(Objects.equals(id, barInst.tryGetId()))
                return ClassInstance.allocateUninitialized(id);
            else if(Objects.equals(id, quxInst.tryGetId()))
                return quxInst;
            else
                throw new InternalException(String.format("Invalid id %s", id));
        };
        TypeDefProvider typeDefProvider = id -> {
            if(fooType.idEquals(id))
                return fooType;
            if(barType.idEquals(id))
                return barType;
            if(quxType.idEquals(id))
                return quxType;
            throw new NullPointerException("Can not find type def for id: " + id);
        };
        var bytes = InstanceOutput.toBytes(fooInst);
        var input = new InstanceInput(new ByteArrayInputStream(bytes), resolveInst, i -> {}, typeDefProvider, id -> null);
        var recoveredFooInst = (ClassInstance) input.readSingleMessageGrove();
        MatcherAssert.assertThat(recoveredFooInst, InstanceMatcher.of(fooInst));
        new StreamVisitor(new ByteArrayInputStream(bytes)) {
        }.visitGrove();
    }

}