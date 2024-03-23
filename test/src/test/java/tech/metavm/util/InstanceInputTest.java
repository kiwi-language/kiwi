package tech.metavm.util;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import tech.metavm.entity.StandardTypes;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.function.Function;

public class InstanceInputTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        StandardTypes.setBooleanType(new PrimitiveType(PrimitiveKind.BOOLEAN));
        StandardTypes.setLongType(new PrimitiveType(PrimitiveKind.LONG));
        StandardTypes.setStringType(new PrimitiveType(PrimitiveKind.STRING));
        StandardTypes.setDoubleType(new PrimitiveType(PrimitiveKind.DOUBLE));
        StandardTypes.setNullType(new PrimitiveType(PrimitiveKind.NULL));
    }

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
        ClassType fooType = ClassTypeBuilder.newBuilder("Foo", "Foo").build();
        ClassType barType = ClassTypeBuilder.newBuilder("Bar", "Bar").build();
        ClassType quxType = ClassTypeBuilder.newBuilder("Qux", "Qux").build();

        fooType.initId(DefaultPhysicalId.of(10001L, 0L, TaggedPhysicalId.ofClass(1L, 0L)));
        barType.initId(DefaultPhysicalId.of(10002L, 0L, TaggedPhysicalId.ofClass(1L, 0L)));
        quxType.initId(DefaultPhysicalId.of(10003L, 0L, TaggedPhysicalId.ofClass(1L, 0L)));

        Field nameField = FieldBuilder
                .newBuilder("name", "name", fooType, StandardTypes.getStringType()).build();
        nameField.initId(DefaultPhysicalId.of(20001L, 0L, TaggedPhysicalId.ofClass(1L, 0L)));
        Field barField = FieldBuilder
                .newBuilder("bar", "bar", fooType, barType).isChild(true).build();
        barField.initId(DefaultPhysicalId.of(20002L, 0L, TaggedPhysicalId.ofClass(1L, 0L)));
        Field quxField = FieldBuilder.newBuilder("qux", "qux", fooType, quxType).build();
        quxField.initId(DefaultPhysicalId.of(20003L, 0L, TaggedPhysicalId.ofClass(1L, 0L)));

        Field barCodeField = FieldBuilder
                .newBuilder("code", "code", barType, StandardTypes.getStringType()).build();
        barCodeField.initId(DefaultPhysicalId.of(20004L, 0L, TaggedPhysicalId.ofClass(1L, 0L)));

        Field quxNameField = FieldBuilder
                .newBuilder("name", "name", quxType, StandardTypes.getStringType()).build();
        quxNameField.initId(DefaultPhysicalId.of(20005L, 0L, TaggedPhysicalId.ofClass(1L, 0L)));

        var barInst = new ClassInstance(
                DefaultPhysicalId.of(30002L, 0L, TaggedPhysicalId.ofClass(1L, 0L)),
                Map.of(
                        barCodeField,
                        new StringInstance(barCode, StandardTypes.getStringType())
                ),
                barType
        );

        var quxInst = new ClassInstance(
                DefaultPhysicalId.of(30003L, 0L, TaggedPhysicalId.ofClass(1L, 0L)),
                Map.of(
                        quxNameField,
                        new StringInstance("qux001", StandardTypes.getStringType())
                ),
                quxType
        );

        var fooInst = new ClassInstance(
                DefaultPhysicalId.of(30001L, 0L, TaggedPhysicalId.ofClass(1L, 0L)),
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
                return new ClassInstance(id, fooType, false, null);
            else if(Objects.equals(id, barInst.tryGetId()))
                return new ClassInstance(id, barType, false, null);
            else if(Objects.equals(id, quxInst.tryGetId()))
                return quxInst;
            else
                throw new InternalException(String.format("Invalid id %s", id));
        };

        var instanceMap = new HashMap<Long, Instance>();
        instanceMap.put(fooInst.getPhysicalId(), fooInst);
        instanceMap.put(barInst.getPhysicalId(), barInst);
        var bytes = InstanceOutput.toMessage(fooInst);
        var input = new InstanceInput(new ByteArrayInputStream(bytes), resolveInst);
        var recoveredFooInst = input.readMessage();
        MatcherAssert.assertThat(recoveredFooInst, InstanceMatcher.of(fooInst));

        new StreamVisitor(new ByteArrayInputStream(bytes)) {
        }.visitMessage();

    }



}