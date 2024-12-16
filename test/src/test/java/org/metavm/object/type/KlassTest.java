package org.metavm.object.type;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.mocks.MockEntityRepository;
import org.metavm.flow.*;
import org.metavm.util.Instances;
import org.metavm.util.TestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Objects;

@Slf4j
public class KlassTest extends TestCase {

    public void testIO() {
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
            Nodes.getProperty(valueField.getRef(), code);
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
            Nodes.newObject(code, pKlass.getMethod(MethodRef::isConstructor), false, false);
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
            if2.setTarget(Nodes.loadConstant(Instances.longInstance(-1), code));
            Nodes.ret(code);
            if1.setTarget(Nodes.loadConstant(Instances.longZero(), code));
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
            Nodes.newObject(code, nameIndexConstructor.getRef(), true, true);
            Nodes.ret(code);
        }
        var index = new org.metavm.object.type.Index(fooKlass, "nameIndex", "", true,
                List.of(), nameIndexMethod);
        new IndexField(index, "name", Types.getStringType(), Values.nullValue());
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
        Assert.assertFalse(factoryMethod.getRef().isParameterized());
        byte[] bytes;
        try (var serContext = SerializeContext.enter()) {
            var bout = new ByteArrayOutputStream();
            var out = new KlassOutput(bout, serContext);
            fooKlass.write(out);
            nameIndexKlass.write(out);
            supplierKlass.write(out);
            bytes = bout.toByteArray();
        }

        var repo = new MockEntityRepository();
        var in = new KlassInput(new ByteArrayInputStream(bytes), repo);
        var k = in.readKlass();
        var nk = in.readKlass();
        in.readKlass();
        Objects.requireNonNull(k);

        Assert.assertNotSame(fooKlass, k);
        Assert.assertEquals(fooKlass.getName(), k.getName());
        Assert.assertEquals(fooKlass.getQualifiedName(), k.getQualifiedName());
        Assert.assertEquals(fooKlass.getFields().size(), k.getFields().size());
        Assert.assertEquals(fooKlass.isSearchable(), k.isSearchable());
        Assert.assertEquals(fooKlass.isStruct(), k.isStruct());
        Assert.assertEquals(fooKlass.isAbstract(), k.isAbstract());
        Assert.assertEquals(fooKlass.isEphemeral(), k.isEphemeral());
        Assert.assertEquals(fooKlass.getTag(), k.getTag());
        Assert.assertEquals(fooKlass.getSourceTag(), k.getSourceTag());
        Assert.assertSame(fooKlass.getSource(), k.getSource());
        Assert.assertEquals(fooKlass.getSince(), k.getSince());

        Assert.assertEquals(fooKlass.getTypeParameters().size(), k.getTypeParameters().size());
        var tp = k.getTypeParameters().get(0);
        Assert.assertEquals(typeParam.getName(), tp.getName());
        Assert.assertEquals(typeParam.getBounds(), tp.getBounds());

        Assert.assertEquals(fooKlass.getFields().size(), k.getFields().size());
        for (Field field : k.getFields()) {
            Assert.assertSame(k, field.getDeclaringType());
        }
        var f = k.getFields().get(0);
        Assert.assertSame(k, f.getDeclaringType());
        Assert.assertEquals(nameField.getName(), f.getName());
        Assert.assertEquals(nameField.getType(), f.getType());
        Assert.assertEquals(nameField.getFlags(), f.getFlags());
        Assert.assertEquals(nameField.getSourceTag(), f.getSourceTag());

        Assert.assertEquals(fooKlass.getMethods().size(), k.getMethods().size());
        for (Method method : k.getMethods()) {
            Assert.assertSame(k, method.getDeclaringType());
        }
        var m1 = k.getMethods().get(0);
        Assert.assertEquals(constructor.getName(), m1.getName());
        Assert.assertEquals(constructor.getFlags(), m1.getFlags());
        Assert.assertEquals(constructor.getConstantPool().size(), m1.getConstantPool().size());
        Assert.assertArrayEquals(constructor.getCode().getCode(), m1.getCode().getCode());
        Assert.assertEquals(k.getType(), m1.getReturnType());
        Assert.assertEquals(constructor.getParameters().size(), m1.getParameters().size());

        var m2 = k.getMethod(m -> m.getName().equals(getComparatorMethod.getName()));
        Assert.assertEquals(getComparatorMethod.getLambdas().size(), m2.getLambdas().size());
        var l = m2.getLambdas().get(0);
        Assert.assertEquals(lambda.getCode().length(), l.getCode().length());

        Assert.assertEquals(fooKlass.getConstraints().size(), k.getConstraints().size());
        for (Constraint constraint : k.getConstraints()) {
            Assert.assertSame(k, constraint.getDeclaringType());
        }
        var idx = k.getConstraints().get(0);
        Assert.assertEquals(index.getName(), idx.getName());

        Assert.assertEquals(nameIndexKlass.isEphemeral(), nk.isEphemeral());

    }

}