package tech.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.object.instance.ColumnKind;
import tech.metavm.util.MockUtils;

import java.util.List;

public class ClassTypeTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
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
        ClassType type1 = ClassTypeBuilder.newBuilder("Foo", null).build();
        ClassType type2 = ClassTypeBuilder.newBuilder("Foo", null).superClass(type1).build();
        Assert.assertTrue(type1.isAssignableFrom(type2));
    }

    public void testIsInstance() {
        var fooTypes = MockUtils.createFooTypes(true);
        var fooInst = MockUtils.createFoo(fooTypes, true);
        Assert.assertTrue(fooTypes.fooType().isInstance(fooInst));
    }

    public void testIsNullable() {
        var fooType = MockUtils.createFooTypes().fooType();
        Assert.assertFalse(fooType.isNullable());
    }

    public void testGetClosure() {
        ClassType i1 = ClassTypeBuilder.newBuilder("i1", null).category(TypeCategory.INTERFACE).build();
        ClassType i2 = ClassTypeBuilder.newBuilder("i2", null).category(TypeCategory.INTERFACE).build();
        ClassType c1 = ClassTypeBuilder.newBuilder("c1", null)
                .interfaces(i1, i2)
                .build();

        ClassType i3 = ClassTypeBuilder.newBuilder("i2", null).category(TypeCategory.INTERFACE).build();
        ClassType c2 = ClassTypeBuilder.newBuilder("c2", null)
                .superClass(c1).interfaces(i3).build();

        ClassType i4 = ClassTypeBuilder.newBuilder("i4", null).category(TypeCategory.INTERFACE).build();

        ClassType c3 = ClassTypeBuilder.newBuilder("c3", null).interfaces(i3)
                .superClass(c2).interfaces(i4)
                .build();

        Assert.assertEquals(4, c3.getRank());

        Assert.assertEquals(List.of(c3, c2, c1, i1, i2, i3, i4), c3.getClosure().getTypes());

        ClassType i5 = ClassTypeBuilder.newBuilder("i5", null).category(TypeCategory.INTERFACE).build();
        i4.setInterfaces(List.of(i5));

        Assert.assertEquals(List.of(c3, c2, c1, i4, i1, i2, i3, i5), c3.getClosure().getTypes());
    }

}