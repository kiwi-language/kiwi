package tech.metavm.util;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import tech.metavm.entity.StandardTypes;
import tech.metavm.management.RegionManager;
import tech.metavm.mocks.Bar;
import tech.metavm.mocks.Baz;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.StringInstance;
import tech.metavm.object.type.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

public class InstanceInputTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        StandardTypes.booleanType = new PrimitiveType(PrimitiveKind.BOOLEAN);
        StandardTypes.longType = new PrimitiveType(PrimitiveKind.LONG);
        StandardTypes.stringType = new PrimitiveType(PrimitiveKind.STRING);
        StandardTypes.doubleType = new PrimitiveType(PrimitiveKind.DOUBLE);
        StandardTypes.nullType = new PrimitiveType(PrimitiveKind.NULL);
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
        ClassType fooType = ClassBuilder.newBuilder("Foo", "Foo").build();
        ClassType barType = ClassBuilder.newBuilder("Bar", "Bar").build();
        ClassType quxType = ClassBuilder.newBuilder("Qux", "Qux").build();

        fooType.initId(10001L);
        barType.initId(10002L);
        quxType.initId(10003L);

        Field nameField = FieldBuilder
                .newBuilder("name", "name", fooType, StandardTypes.stringType).build();
        nameField.initId(20001L);
        Field barField = FieldBuilder
                .newBuilder("bar", "bar", fooType, barType).isChild(true).build();
        barField.initId(20002L);
        Field quxField = FieldBuilder.newBuilder("qux", "qux", fooType, quxType).build();
        quxField.initId(20003L);

        Field barCodeField = FieldBuilder
                .newBuilder("code", "code", barType, StandardTypes.stringType).build();
        barCodeField.initId(20004L);

        Field quxNameField = FieldBuilder
                .newBuilder("name", "name", quxType, StandardTypes.getStringType()).build();
        quxNameField.initId(20005L);

        var barInst = new ClassInstance(
                30002L,
                Map.of(
                        barCodeField,
                        new StringInstance(barCode, StandardTypes.getStringType())
                ),
                barType
        );

        var quxInst = new ClassInstance(
                30003L,
                Map.of(
                        quxNameField,
                        new StringInstance("qux001", StandardTypes.getStringType())
                ),
                quxType
        );

        var fooInst = new ClassInstance(
                30001L,
                Map.of(
                        nameField, new StringInstance(fooName, StandardTypes.stringType),
                        barField, barInst,
                        quxField, quxInst
                ),
                fooType
        );
        barInst.resetParent(fooInst, barField);

        Function<Long, Instance> resolveInst = id -> {
            if(Objects.equals(id, fooInst.getIdRequired()))
                return new ClassInstance(id, fooType, null);
            else if(Objects.equals(id, barInst.getIdRequired()))
                return new ClassInstance(id, barType, null);
            else if(Objects.equals(id, quxInst.getIdRequired()))
                return quxInst;
            else
                throw new InternalException(String.format("Invalid id %d", id));
        };

        Map<Long, Instance> instanceMap = new HashMap<>();
        instanceMap.put(fooInst.getIdRequired(), fooInst);
        instanceMap.put(barInst.getIdRequired(), barInst);
        var bytes = InstanceOutput.toMessage(fooInst);
        var input = new InstanceInput(new ByteArrayInputStream(bytes), resolveInst);
        var recoveredFooInst = input.readMessage();
        MatcherAssert.assertThat(recoveredFooInst, InstanceMatcher.of(fooInst));

        new StreamVisitor(new ByteArrayInputStream(bytes)) {
        }.visitMessage();

    }



}