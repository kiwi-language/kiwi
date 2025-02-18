package org.metavm.object.type;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.entity.DummyGenericDeclaration;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.NameAndType;
import org.metavm.object.instance.ColumnKind;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.util.InstanceInput;
import org.metavm.util.InstanceOutput;
import org.metavm.util.MockUtils;
import org.metavm.util.TestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Slf4j
public class ClassTypeTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        TestUtils.ensureStringKlassInitialized();
        MockStandardTypesInitializer.init();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAllocateColumn() {
        var fooTypes = MockUtils.createFooTypes(true);
        Field nameField = fooTypes.fooNameField();
        Field bazField = fooTypes.fooQuxField();

        Assert.assertNotNull(nameField.getColumn());
        Assert.assertTrue(nameField.getColumnName().startsWith(ColumnKind.STRING.prefix()));

        Assert.assertNotNull(bazField.getColumn());
        Assert.assertTrue(bazField.getColumnName().startsWith(ColumnKind.REFERENCE.prefix()));
    }

    public void testAllocateColumnForArray() {
        var fooTypes = MockUtils.createFooTypes(true);
        Assert.assertNotNull(fooTypes.fooBarsField().getColumn());
        Assert.assertTrue(fooTypes.fooBarsField().getColumnName().startsWith(ColumnKind.REFERENCE.prefix()));
    }

    public void testIsAssignable() {
        Klass type1 = TestUtils.newKlassBuilder("Foo", null).build();
        Klass type2 = TestUtils.newKlassBuilder("Foo", null).superType(type1.getType()).build();
        Assert.assertTrue(type1.isAssignableFrom(type2));
    }

    public void testIsInstance() {
        var fooTypes = MockUtils.createFooTypes(true);
        var fooInst = MockUtils.createFoo(fooTypes);
        Assert.assertTrue(fooTypes.fooType().getType().isInstance(fooInst.getReference()));
    }

    public void testIsNullable() {
        var fooType = MockUtils.createFooTypes().fooType();
        Assert.assertFalse(fooType.getType().isNullable());
    }

    public void testGetClosure() {
        Klass i1 = TestUtils.newKlassBuilder("i1", null).kind(ClassKind.INTERFACE).build();
        Klass i2 = TestUtils.newKlassBuilder("i2", null).kind(ClassKind.INTERFACE).build();
        Klass c1 = TestUtils.newKlassBuilder("c1", null)
                .interfaces(i1.getType(), i2.getType())
                .build();

        Klass i3 = TestUtils.newKlassBuilder("i2", null).kind(ClassKind.INTERFACE).build();
        Klass c2 = TestUtils.newKlassBuilder("c2", null)
                .superType(c1.getType()).interfaces(i3.getType()).build();

        Klass i4 = TestUtils.newKlassBuilder("i4", null).kind(ClassKind.INTERFACE).build();

        Klass c3 = TestUtils.newKlassBuilder("c3", null).interfaces(i3.getType())
                .superType(c2.getType()).interfaces(i4.getType())
                .build();

        Assert.assertEquals(4, c3.getRank());

        Assert.assertEquals(List.of(c3, c2, c1, i1, i2, i3, i4), c3.getClosure().getClasses());

        Klass i5 = TestUtils.newKlassBuilder("i5", null).kind(ClassKind.INTERFACE).build();
        i4.setInterfaces(List.of(i5.getType()));

        Assert.assertEquals(List.of(c3, c2, c1, i4, i1, i2, i3, i5), c3.getClosure().getClasses());
    }

    public void testResolveMethod() {
        var baseKlass = TestUtils.newKlassBuilder("Base", "Base").build();

        MethodBuilder.newBuilder(baseKlass, "test")
                .parameters(new NameAndType("p1", Types.getStringType()))
                .build();

        var m1 = MethodBuilder.newBuilder(baseKlass, "test")
                .parameters(new NameAndType("p1", Types.getBooleanType()))
                .build();

        var fooKlass = TestUtils.newKlassBuilder("Foo", "Foo")
                .superType(baseKlass.getType())
                .build();

        var m2 = MethodBuilder.newBuilder(fooKlass, "test")
                .parameters(new NameAndType("p1", Types.getAnyType()))
                .build();

        var m3 = MethodBuilder.newBuilder(fooKlass, "test")
                .parameters(new NameAndType("p1", Types.getStringType()))
                .build();

        var m4 = MethodBuilder.newBuilder(fooKlass, "test")
                .parameters(new NameAndType("p1", Types.getDoubleType()))
                .build();

        var fooType = fooKlass.getType();
        Assert.assertSame(m1, fooType.resolveMethod("test", List.of(Types.getBooleanType()), List.of(), false).getRawFlow());
        Assert.assertSame(m2, fooType.resolveMethod("test", List.of(Types.getLongType()), List.of(), false).getRawFlow());
        Assert.assertSame(m3, fooType.resolveMethod("test", List.of(Types.getStringType()), List.of(), false).getRawFlow());
    }

    public void testNestedParameterizedType() {
        var listKlass = TestUtils.newKlassBuilder("List").build();
        listKlass.setTypeParameters(List.of(
                new TypeVariable(listKlass.nextChildId(), "E", listKlass)
        ));
        var fooKlass = TestUtils.newKlassBuilder("Foo").build();
        fooKlass.setTypeParameters(List.of(
                new TypeVariable(fooKlass.nextChildId(), "K", fooKlass),
                new TypeVariable(fooKlass.nextChildId(), "V", fooKlass)
        ));
        var entryKlass = TestUtils.newKlassBuilder("Entry").build();
        entryKlass.setTypeParameters(List.of(
                new TypeVariable(entryKlass.nextChildId(), "K", entryKlass),
                new TypeVariable(entryKlass.nextChildId(), "V", entryKlass)
        ));
        var classType = new KlassType(
                null,
                listKlass,
                List.of(
                        new KlassType(
                                null,
                                entryKlass,
                                fooKlass.getDefaultTypeArguments()
                        )
                )
        );
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        classType.write(output);
        var input = new InstanceInput(new ByteArrayInputStream(bout.toByteArray()));
        var typeKey = TypeKey.read(input);
        Assert.assertEquals(classType.toTypeKey(ITypeDef::getId), typeKey);
    }

}