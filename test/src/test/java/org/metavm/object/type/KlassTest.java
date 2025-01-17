package org.metavm.object.type;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.mocks.MockEntityRepository;
import org.metavm.flow.*;
import org.metavm.object.instance.core.Id;
import org.metavm.util.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
public class KlassTest extends TestCase {

    public void testIO() {
        var testKlasses = createTestKlasses();
        TestUtils.initEntityTmpIds(testKlasses);
        var fooKlass = testKlasses.getFirst();
        var nameIndexKlass = testKlasses.get(1);
        var supplierKlass = testKlasses.get(2);

//        Assert.assertFalse(factoryMethod.getRef().isParameterized());
        byte[] bytes;
        try (var serContext = SerializeContext.enter()) {
            var bout = new ByteArrayOutputStream();
            var out = new KlassOutput(bout, serContext);
            testKlasses.forEach(out::writeEntity);
            bytes = bout.toByteArray();
        }

        var repo = new MockEntityRepository();
        var in = new KlassInput(new ByteArrayInputStream(bytes), repo);
        var k = in.readEntity(Klass.class, null);
        var nk = in.readEntity(Klass.class, null);
        in.readEntity(Klass.class, null);
        Objects.requireNonNull(k);

        Assert.assertNotSame(fooKlass, k);
        Assert.assertEquals(fooKlass.getName(), k.getName());
        Assert.assertEquals(fooKlass.getQualifiedName(), k.getQualifiedName());
        Assert.assertEquals(fooKlass.getFields().size(), k.getFields().size());
        Assert.assertEquals(fooKlass.isSearchable(), k.isSearchable());
        Assert.assertEquals(fooKlass.isStruct(), k.isStruct());
        Assert.assertEquals(fooKlass.isAbstract(), k.isAbstract());
        Assert.assertEquals(fooKlass.isEphemeralKlass(), k.isEphemeralKlass());
        Assert.assertEquals(fooKlass.getTag(), k.getTag());
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
        Assert.assertEquals(nameField.getSourceTag(), f.getSourceTag());

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
        Assert.assertEquals(k.getType(), m1.getReturnType());
        Assert.assertEquals(constructor.getParameters().size(), m1.getParameters().size());

        var getComparatorMethod = fooKlass.getMethod(m -> m.getName().equals("getComparator"));
        var lambda = getComparatorMethod.getLambdas().getFirst();
        var m2 = k.getMethod(m -> m.getName().equals(getComparatorMethod.getName()));
        Assert.assertEquals(getComparatorMethod.getLambdas().size(), m2.getLambdas().size());
        var l = m2.getLambdas().getFirst();
        Assert.assertEquals(lambda.getCode().length(), l.getCode().length());

        Assert.assertEquals(fooKlass.getIndices().size(), k.getIndices().size());
        for (Constraint constraint : k.getIndices()) {
            Assert.assertSame(k, constraint.getDeclaringType());
        }
        var index = fooKlass.getIndices().getFirst();
        var idx = k.getIndices().getFirst();
        Assert.assertEquals(index.getName(), idx.getName());
        Assert.assertEquals(nameIndexKlass.isEphemeralKlass(), nk.isEphemeralKlass());
    }

    public void testInstanceIO() {
        var testKlasses = createTestKlasses();
        var fooKlass = testKlasses.getFirst();
        var nameIndexKlass = testKlasses.get(1);
        var supplierKlass = testKlasses.get(2);

        TestUtils.initEntityIds(fooKlass);
        Assert.assertNotNull(nameIndexKlass.tryGetId());
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
        var k1 = in.readEntity(Klass.class, null);
        var k2 = in.readEntity(Klass.class, null);
        var k3 = in.readEntity(Klass.class, null);
        Assert.assertEquals(fooKlass.getName(), k1.getName());
        Assert.assertEquals(nameIndexKlass.getName(), k2.getName());
        Assert.assertEquals(supplierKlass.getName(), k3.getName());

        var ref = new Object() {
            int klassCount;
            int referenceCount;
            int classTypeCount;
            int methodCount;
        };

        var visitor = new StreamVisitor(new ByteArrayInputStream(bout.toByteArray())) {
            @Override
            public void visitEntityBody(int tag, Id id) {
                if (tag == EntityRegistry.TAG_Klass)
                    ref.klassCount++;
                else if (tag == EntityRegistry.TAG_Method)
                    ref.methodCount++;
                super.visitEntityBody(tag, id);
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
        visitor.visitEntity();
        Assert.assertEquals(3, ref.klassCount);
        log.info("Reference count: {}", ref.referenceCount);
        log.info("Method count: {}", ref.methodCount);
        log.info("ClassType count: {}", ref.classTypeCount);
    }

    public void testRebuildNodes() throws IOException {
        var klasses = createTestKlasses();
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

    private List<Klass> createTestKlasses() {
        var supplierKlass = TestUtils.newKlassBuilder("Supplier").kind(ClassKind.INTERFACE).build();
        var supplierTypeParam = new TypeVariable(null, "T", supplierKlass);
        MethodBuilder.newBuilder(supplierKlass, "get")
                .returnType(supplierTypeParam.getType())
                .isAbstract(true)
                .build();
        var fooKlass = TestUtils.newKlassBuilder("Foo").searchable(true).build();
        var typeParam = new TypeVariable(null, "T", fooKlass);
        fooKlass.setInterfaces(List.of(KlassType.create(supplierKlass, List.of(typeParam.getType()))));
        var nameField = FieldBuilder.newBuilder("name", fooKlass, Types.getStringType()).build();
        var valueField = FieldBuilder.newBuilder("value", fooKlass, typeParam.getType()).build();

        var constructor = MethodBuilder.newBuilder(fooKlass, "Foo")
                .isConstructor(true)
                .parameters(
                        new NameAndType("name", Types.getStringType()),
                        new NameAndType("value", typeParam.getType())
                )
                .build();
        {
            var code = constructor.getCode();
            Nodes.this_(code);
            Nodes.argument(constructor, 0);
            Nodes.setField(nameField.getRef(), code);
            Nodes.this_(code);
            Nodes.argument(constructor, 1);
            Nodes.setField(valueField.getRef(), code);
            Nodes.this_(code);
            Nodes.ret(code);
        }
        var getMethod = MethodBuilder.newBuilder(fooKlass, "get")
                .returnType(typeParam.getType())
                .build();
        {
            var code = getMethod.getCode();
            Nodes.this_(code);
            Nodes.getField(valueField.getRef(), code);
            Nodes.ret(code);
        }
        var factoryMethod = MethodBuilder.newBuilder(fooKlass, "create").isStatic(true).build();
        var factoryTypeParam = new TypeVariable(null, "T", factoryMethod);
        var pKlass = KlassType.create(fooKlass, List.of(factoryTypeParam.getType()));
        factoryMethod.setParameters(List.of(
                new Parameter(null, "name", Types.getStringType(), factoryMethod),
                new Parameter(null, "value", factoryTypeParam.getType(), factoryMethod)
        ));
        factoryMethod.setReturnType(pKlass);
        {
            var code = factoryMethod.getCode();
            Nodes.argument(factoryMethod, 0);
            Nodes.argument(factoryMethod, 1);
            Nodes.newObject(code, pKlass, false, false);
            Nodes.invokeMethod(pKlass.getMethod(MethodRef::isConstructor), code);
            Nodes.ret(code);
        }
        var longFooKlass = KlassType.create(fooKlass, List.of(Types.getLongType()));
        var getComparatorMethod = MethodBuilder.newBuilder(fooKlass, "getComparator")
                .isStatic(true)
                .returnType(new FunctionType(List.of(longFooKlass, longFooKlass), Types.getLongType()))
                .build();
        var lambda = new Lambda(null, List.of(), Types.getLongType(), getComparatorMethod);
        lambda.setParameters(List.of(
                new Parameter(null, "foo1", longFooKlass, lambda),
                new Parameter(null, "foo2", longFooKlass, lambda)
        ));
        {
            var code = lambda.getCode();
            Nodes.argument(lambda, 0);
            Nodes.argument(lambda, 1);
            Nodes.compareEq(Types.getLongType(), code);
            var if1 = Nodes.ifNe(null, code);
            Nodes.argument(lambda, 0);
            Nodes.argument(lambda, 1);
            Nodes.lt(code);
            var if2 = Nodes.ifNe(null, code);
            Nodes.loadConstant(Instances.longOne(), code);
            Nodes.ret(code);
            if2.setTarget(Nodes.label(code));
            Nodes.loadConstant(Instances.longInstance(-1), code);
            Nodes.ret(code);
            if1.setTarget(Nodes.label(code));
            Nodes.loadConstant(Instances.longZero(), code);
            Nodes.ret(code);
        }
        {
            var code = getComparatorMethod.getCode();
            Nodes.lambda(lambda, code);
            Nodes.ret(code);
        }
        var nameIndexKlass = TestUtils.newKlassBuilder("NameIndex", "NameIndex").ephemeral(true).build();
        var indexNameField = FieldBuilder.newBuilder("name", nameIndexKlass, Types.getStringType()).build();
        var nameIndexConstructor = MethodBuilder.newBuilder(nameIndexKlass, "NameIndex")
                .isConstructor(true)
                .parameters(new NameAndType("name", Types.getStringType()))
                .build();
        {
            var code = nameIndexConstructor.getCode();
            Nodes.this_(code);
            Nodes.argument(nameIndexConstructor, 0);
            Nodes.setField(indexNameField.getRef(), code);
            Nodes.this_(code);
            Nodes.ret(code);
        }
        var nameIndexMethod = MethodBuilder.newBuilder(fooKlass, "nameIndex")
                .parameters(new NameAndType("name", Types.getStringType()))
                .returnType(nameIndexKlass.getType())
                .build();
        {
            var code = nameIndexMethod.getCode();
            Nodes.argument(nameIndexMethod, 0);
            Nodes.newObject(code, nameIndexKlass.getType(), true, true);
            Nodes.invokeMethod(nameIndexConstructor.getRef(), code);
            Nodes.ret(code);
        }
        var index = new org.metavm.object.type.Index(fooKlass, "nameIndex", "", true,
                List.of(), nameIndexMethod);
        new IndexField(index, "name", Types.getStringType(), null);
        var getByNameMethod = MethodBuilder.newBuilder(fooKlass, "getByName")
                .isStatic(true)
                .parameters(new NameAndType("name", Types.getStringType()))
                .returnType(fooKlass.getType())
                .build();
        {
            var code = getByNameMethod.getCode();
            Nodes.argument(getByNameMethod, 0);
            Nodes.selectFirst(index, code);
            Nodes.nonNull(code);
            Nodes.ret(code);
        }
        fooKlass.emitCode();
        nameIndexKlass.emitCode();
        return List.of(fooKlass, nameIndexKlass, supplierKlass);

    }

}