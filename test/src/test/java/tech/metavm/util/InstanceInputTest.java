package tech.metavm.util;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import tech.metavm.entity.StandardTypes;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

public class InstanceInputTest extends TestCase {

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

    public void test() {
        String fooName = "foo", barCode = "bar001";
        Klass fooType = ClassTypeBuilder.newBuilder("Foo", "Foo").build();
        Klass barType = ClassTypeBuilder.newBuilder("Bar", "Bar").build();
        Klass quxType = ClassTypeBuilder.newBuilder("Qux", "Qux").build();

        fooType.initId(PhysicalId.of(10001L, 0L, TestUtils.mockClassType()));
        barType.initId(PhysicalId.of(10002L, 0L, TestUtils.mockClassType()));
        quxType.initId(PhysicalId.of(10003L, 0L, TestUtils.mockClassType()));

        Field nameField = FieldBuilder
                .newBuilder("name", "name", fooType, StandardTypes.getStringType()).build();
        nameField.initId(PhysicalId.of(10001L, 1L, TestUtils.mockClassType()));
        Field barField = FieldBuilder
                .newBuilder("bar", "bar", fooType, barType.getType()).isChild(true).build();
        barField.initId(PhysicalId.of(10001L, 2L, TestUtils.mockClassType()));
        Field quxField = FieldBuilder.newBuilder("qux", "qux", fooType, quxType.getType()).build();
        quxField.initId(PhysicalId.of(10001L, 3L, TestUtils.mockClassType()));

        Field barCodeField = FieldBuilder
                .newBuilder("code", "code", barType, StandardTypes.getStringType()).build();
        barCodeField.initId(PhysicalId.of(10002L, 1L, TestUtils.mockClassType()));

        Field quxNameField = FieldBuilder
                .newBuilder("name", "name", quxType, StandardTypes.getStringType()).build();
        quxNameField.initId(PhysicalId.of(10003L, 1L, TestUtils.mockClassType()));

        var barInst = new ClassInstance(
                PhysicalId.of(30001L, 1L, TestUtils.mockClassType()),
                Map.of(
                        barCodeField,
                        new StringInstance(barCode, StandardTypes.getStringType())
                ),
                barType
        );

        var quxInst = new ClassInstance(
                PhysicalId.of(30002L, 0L, TestUtils.mockClassType()),
                Map.of(
                        quxNameField,
                        new StringInstance("qux001", StandardTypes.getStringType())
                ),
                quxType
        );

        var fooInst = new ClassInstance(
                PhysicalId.of(30001L, 0L, TestUtils.mockClassType()),
                Map.of(
                        nameField, new StringInstance(fooName, StandardTypes.getStringType()),
                        barField, barInst,
                        quxField, quxInst
                ),
                fooType
        );
        barInst.setParentInternal(fooInst, barField);

        Function<Id, DurableInstance> resolveInst = id -> {
            if(Objects.equals(id, fooInst.tryGetId()))
                return new ClassInstance(id, fooType.getType(), false, null);
            else if(Objects.equals(id, barInst.tryGetId()))
                return new ClassInstance(id, barType.getType(), false, null);
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
        var input = new InstanceInput(new ByteArrayInputStream(bytes), resolveInst, InstanceInput.UNSUPPORTED_ADD_VALUE, typeDefProvider);
        var recoveredFooInst = input.readMessage();
        MatcherAssert.assertThat(recoveredFooInst, InstanceMatcher.of(fooInst));
        new StreamVisitor(new ByteArrayInputStream(bytes)) {
        }.visitMessage();
    }

}