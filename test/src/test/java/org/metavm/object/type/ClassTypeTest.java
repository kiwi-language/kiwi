package org.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.entity.StandardTypes;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Parameter;
import org.metavm.object.instance.ColumnKind;
import org.metavm.util.MockUtils;

import java.util.List;

public class ClassTypeTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
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
        Klass type1 = KlassBuilder.newBuilder("Foo", null).build();
        Klass type2 = KlassBuilder.newBuilder("Foo", null).superClass(type1.getType()).build();
        Assert.assertTrue(type1.isAssignableFrom(type2));
    }

    public void testIsInstance() {
        var fooTypes = MockUtils.createFooTypes(true);
        var fooInst = MockUtils.createFoo(fooTypes, true);
        Assert.assertTrue(fooTypes.fooType().getType().isInstance(fooInst));
    }

    public void testIsNullable() {
        var fooType = MockUtils.createFooTypes().fooType();
        Assert.assertFalse(fooType.getType().isNullable());
    }

    public void testGetClosure() {
        Klass i1 = KlassBuilder.newBuilder("i1", null).kind(ClassKind.INTERFACE).build();
        Klass i2 = KlassBuilder.newBuilder("i2", null).kind(ClassKind.INTERFACE).build();
        Klass c1 = KlassBuilder.newBuilder("c1", null)
                .interfaces(i1.getType(), i2.getType())
                .build();

        Klass i3 = KlassBuilder.newBuilder("i2", null).kind(ClassKind.INTERFACE).build();
        Klass c2 = KlassBuilder.newBuilder("c2", null)
                .superClass(c1.getType()).interfaces(i3.getType()).build();

        Klass i4 = KlassBuilder.newBuilder("i4", null).kind(ClassKind.INTERFACE).build();

        Klass c3 = KlassBuilder.newBuilder("c3", null).interfaces(i3.getType())
                .superClass(c2.getType()).interfaces(i4.getType())
                .build();

        Assert.assertEquals(4, c3.getRank());

        Assert.assertEquals(List.of(c3, c2, c1, i1, i2, i3, i4), c3.getClosure().getClasses());

        Klass i5 = KlassBuilder.newBuilder("i5", null).kind(ClassKind.INTERFACE).build();
        i4.setInterfaces(List.of(i5.getType()));

        Assert.assertEquals(List.of(c3, c2, c1, i4, i1, i2, i3, i5), c3.getClosure().getClasses());
    }

    public void testResolveMethod() {
        var baseType = KlassBuilder.newBuilder("Base", "Base").build();

        MethodBuilder.newBuilder(baseType, "test", "test")
                .parameters(new Parameter(null, "p1", "p1", StandardTypes.getStringType()))
                .build();

        var m1 = MethodBuilder.newBuilder(baseType, "test", "test")
                .parameters(new Parameter(null, "p1", "p1", StandardTypes.getBooleanType()))
                .build();

        var fooType = KlassBuilder.newBuilder("Foo", "Foo")
                .superClass(baseType.getType())
                .build();

        var m2 = MethodBuilder.newBuilder(fooType, "test", "test")
                .parameters(new Parameter(null, "p1", "p1", StandardTypes.getAnyType()))
                .build();

        var m3 = MethodBuilder.newBuilder(fooType, "test", "test")
                .parameters(new Parameter(null, "p1", "p1", StandardTypes.getStringType()))
                .build();

        MethodBuilder.newBuilder(fooType, "test", "test")
                .parameters(new Parameter(null, "p1", "p1", StandardTypes.getDoubleType()))
                .build();

        Assert.assertSame(m1, fooType.resolveMethod("test", List.of(StandardTypes.getBooleanType()), List.of(), false));
        Assert.assertSame(m2, fooType.resolveMethod("test", List.of(StandardTypes.getLongType()), List.of(), false));
        Assert.assertSame(m3, fooType.resolveMethod("test", List.of(StandardTypes.getStringType()), List.of(), false));
    }

}