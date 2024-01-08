package tech.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.ColumnKind;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;

import java.util.List;

public class ClassTypeTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockRegistry.setUp(new MockIdProvider());
    }

    public void testAllocateColumn() {
        ClassType fooType = ClassTypeBuilder.newBuilder("Foo", null).build();
        ClassType barType = ClassTypeBuilder.newBuilder("Bar", null).build();

        Field nameField = FieldBuilder
                .newBuilder("name", null, fooType, MockRegistry.getStringType())
                .build();
        fooType.setTitleField(nameField);


        Field barField = FieldBuilder
                .newBuilder("bar", null, fooType, barType)
                .build();

        Assert.assertNotNull(nameField.getColumn());
        Assert.assertTrue(nameField.getColumnName().startsWith(ColumnKind.STRING.prefix()));

        Assert.assertNotNull(barField.getColumn());
        Assert.assertTrue(barField.getColumnName().startsWith(ColumnKind.UNSPECIFIED.prefix()));
    }

    public void testAllocateColumnForArray() {
        ClassType bazType = ClassTypeBuilder.newBuilder("Baz", null).build();
        ClassType barType = ClassTypeBuilder.newBuilder("Bar", null).build();
        Field barsField = FieldBuilder
                .newBuilder("bars", null, bazType, new ArrayType(null, barType, ArrayKind.READ_WRITE))
                .build();
        Assert.assertNotNull(barsField.getColumn());
        Assert.assertTrue(barsField.getColumnName().startsWith(ColumnKind.REFERENCE.prefix()));
    }

    public void testIsAssignable() {
        ClassType type1 = ClassTypeBuilder.newBuilder("Foo", null).build();
        ClassType type2 = ClassTypeBuilder.newBuilder("Foo", null).superClass(type1).build();
        Assert.assertTrue(type1.isAssignableFrom(type2));
    }

    public void testIsInstance() {
        ClassInstance fooInst = MockRegistry.getFooInstance();
        ClassType fooType = MockRegistry.getClassType(Foo.class);
        Assert.assertTrue(fooType.isInstance(fooInst));
    }

    public void testIsNullable() {
        ClassType fooType = MockRegistry.getClassType(Foo.class);
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

        ClassType c3  = ClassTypeBuilder.newBuilder("c3", null).interfaces(i3)
                .superClass(c2).interfaces(i4)
                .build();

        Assert.assertEquals(4, c3.getRank());

        Assert.assertEquals(List.of(c3, c2, c1, i1, i2, i3, i4), c3.getClosure().getTypes());

        ClassType i5 = ClassTypeBuilder.newBuilder("i5", null).category(TypeCategory.INTERFACE).build();
        i4.setInterfaces(List.of(i5));

        Assert.assertEquals(List.of(c3, c2, c1, i4, i1, i2, i3, i5), c3.getClosure().getTypes());
    }

}