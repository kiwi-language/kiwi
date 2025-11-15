package org.metavm.object.type;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.classfile.ClassFileReader;
import org.metavm.classfile.ClassFileWriter;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.StdKlass;
import org.metavm.entity.mocks.MockEntityRepository;
import org.metavm.flow.KlassInput;
import org.metavm.flow.KlassOutput;
import org.metavm.flow.Method;
import org.metavm.util.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Objects;

@Slf4j
public class KlassTest extends TestCase {

    public void testIO() {
        var testKlasses = MockUtils.createTestKlasses();
        var fooKlass = testKlasses.getFirst();
        var supplierKlass = testKlasses.get(1);

//        Assert.assertFalse(factoryMethod.getRef().isParameterized());
        byte[] bytes;
        try (var ignored = SerializeContext.enter()) {
            var bout = new ByteArrayOutputStream();
            var out = new KlassOutput(bout);
            var classFileWriter = new ClassFileWriter(out);
            testKlasses.forEach(classFileWriter::write);
            bytes = bout.toByteArray();
        }

        var repo = new MockEntityRepository();
        repo.bind(StdKlass.string.get());
        repo.bind(StdKlass.index.get());

        var reader = new ClassFileReader(new KlassInput(new ByteArrayInputStream(bytes), repo), repo, null);
        var k = reader.read();
        reader.read();
        Objects.requireNonNull(k);

        Assert.assertNotSame(fooKlass, k);
        Assert.assertEquals(fooKlass.getName(), k.getName());
        Assert.assertEquals(fooKlass.getQualifiedName(), k.getQualifiedName());
        Assert.assertEquals(fooKlass.getFields().size(), k.getFields().size());
        Assert.assertEquals(fooKlass.isSearchable(), k.isSearchable());
        Assert.assertEquals(fooKlass.isStruct(), k.isStruct());
        Assert.assertEquals(fooKlass.isAbstract(), k.isAbstract());
        Assert.assertEquals(fooKlass.isEphemeralKlass(), k.isEphemeralKlass());
//        Assert.assertEquals(fooKlass.getTag(), k.getTag());
        Assert.assertEquals(fooKlass.getSourceTag(), k.getSourceTag());
        Assert.assertSame(fooKlass.getSource(), k.getSource());
        Assert.assertEquals(fooKlass.getSince(), k.getSince());

        Assert.assertEquals(fooKlass.getTypeParameters().size(), k.getTypeParameters().size());
        var tp = k.getTypeParameters().getFirst();
        var typeParam = fooKlass.getTypeParameters().getFirst();
        Assert.assertEquals(typeParam.getName(), tp.getName());
        Assert.assertEquals(typeParam.getBounds(), tp.getBounds());

        Assert.assertEquals(fooKlass.getFields().size(), k.getFields().size());
        for (Field field : k.getFields()) {
            Assert.assertSame(k, field.getDeclaringType());
        }

        var nameField = fooKlass.getFields().getFirst();
        var f = k.getFields().getFirst();
        Assert.assertSame(k, f.getDeclaringType());
        Assert.assertEquals(nameField.getName(), f.getName());
        Assert.assertEquals(nameField.getType(), f.getType());
        Assert.assertEquals(nameField.getFlags(), f.getFlags());
        Assert.assertNotNull(f.getSourceTag());

        Assert.assertEquals(fooKlass.getMethods().size(), k.getMethods().size());
        for (Method method : k.getMethods()) {
            Assert.assertSame(k, method.getDeclaringType());
        }
        var constructor = fooKlass.getMethods().getFirst();
        var m1 = k.getMethods().getFirst();
        Assert.assertEquals(constructor.getName(), m1.getName());
        Assert.assertEquals(constructor.getFlags(), m1.getFlags());
        Assert.assertEquals(constructor.getConstantPool().size(), m1.getConstantPool().size());
        Assert.assertArrayEquals(constructor.getCode().getCode(), m1.getCode().getCode());
        Assert.assertEquals(Types.getVoidType(), m1.getReturnType());
        Assert.assertEquals(constructor.getParameters().size(), m1.getParameters().size());

        var getComparatorMethod = fooKlass.getMethod(m -> m.getName().equals("getComparator"));
        var lambda = getComparatorMethod.getLambdas().getFirst();
        var m2 = k.getMethod(m -> m.getName().equals(getComparatorMethod.getName()));
        Assert.assertEquals(getComparatorMethod.getLambdas().size(), m2.getLambdas().size());
        var l = m2.getLambdas().getFirst();
        Assert.assertEquals(lambda.getCode().length(), l.getCode().length());

        Assert.assertEquals(1, k.getIndices().size());
        for (Constraint constraint : k.getIndices()) {
            Assert.assertSame(k, constraint.getDeclaringType());
        }
        var idx = k.getIndices().getFirst();
        Assert.assertEquals("nameIdx", idx.getName());
    }

    public void testInstanceIO() {
        var testKlasses = MockUtils.createTestKlasses();
        var fooKlass = testKlasses.getFirst();
        var supplierKlass = testKlasses.get(1);

        Assert.assertNotNull(supplierKlass.tryGetId());

        var repo = new MockEntityRepository();
        testKlasses.forEach(repo::bind);

        var bout = new ByteArrayOutputStream();
        var out = new InstanceOutput(bout);
        testKlasses.forEach(out::writeEntity);

        var in = new InstanceInput(
                new ByteArrayInputStream(bout.toByteArray()),
                InstanceInput.UNSUPPORTED_RESOLVER,
                i -> {},
                InstanceInput.UNSUPPORTED_REDIRECTION_SIGNAL_PROVIDER
        );
        var k1 = (Klass) in.readEntity();
        var k2 = (Klass) in.readEntity();
        Assert.assertEquals(fooKlass.getName(), k1.getName());
        Assert.assertEquals(supplierKlass.getName(), k2.getName());

        var ref = new Object() {
            int klassCount;
            int referenceCount;
            int classTypeCount;
            int methodCount;
        };

        var visitor = new StreamVisitor(new ByteArrayInputStream(bout.toByteArray())) {

//            @Override
//            public void visitEntityBody(int tag, Id id, int refcount) {
//                if (tag == EntityRegistry.TAG_Klass)
//                    ref.klassCount++;
//                else if (tag == EntityRegistry.TAG_Method)
//                    ref.methodCount++;
//                super.visitEntityBody(tag, id, refcount);
//            }

            @Override
            public void visitEntityHead() {
                var id = readId();
                readInt();
                if (id.isRoot())
                    ref.klassCount++;
            }

            @Override
            public void visitClassType() {
                ref.classTypeCount++;
                super.visitClassType();
            }

            @Override
            public void visitReference() {
                ref.referenceCount++;
                super.visitReference();
            }
        };
        visitor.visitEntity();
        visitor.visitEntity();
        Assert.assertEquals(2, ref.klassCount);
        log.info("Reference count: {}", ref.referenceCount);
        log.info("Method count: {}", ref.methodCount);
        log.info("ClassType count: {}", ref.classTypeCount);
    }

    public void testRebuildNodes() {
        var klasses = MockUtils.createTestKlasses();
        var fooKlass = klasses.getFirst();
        var method = fooKlass.getMethod(Method::isConstructor);
        log.info("{}", EncodingUtils.bytesToHex(method.getCode().getCode()));
        log.info("{}", method.getText());
        method.getCode().clearNodes();
        method.getCode().rebuildNodes();
        log.info("{}", method.getText());
    }

    public void testVisitGraph() {
        var fooKlass = TestUtils.newKlassBuilder("Foo").build();
        var barKlass = TestUtils.newKlassBuilder("Bar").build();
        FieldBuilder.newBuilder("bar", fooKlass, Types.getNullableType(barKlass.getType())).build();
        fooKlass.visitGraph(i -> {
            i.setMarked();
            return true;
        });
        Assert.assertTrue(barKlass.isMarked());
    }

}