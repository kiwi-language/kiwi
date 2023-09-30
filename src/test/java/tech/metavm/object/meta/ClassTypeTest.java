package tech.metavm.object.meta;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.SQLType;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;

public class ClassTypeTest extends TestCase {

    private TypeFactory typeFactory;

    @Override
    protected void setUp() throws Exception {
        MockRegistry.setUp(new MockIdProvider());
        typeFactory = new DefaultTypeFactory(MockRegistry::getType);
    }

    public void testAllocateColumn() {
        ClassType fooType = ClassBuilder.newBuilder("Foo", null).build();
        ClassType barType = ClassBuilder.newBuilder("Bar", null).build();

        Field nameField = FieldBuilder
                .newBuilder("name", null, fooType, MockRegistry.getStringType())
                .asTitle(true)
                .build();

        Field barField = FieldBuilder
                .newBuilder("bar", null, fooType, barType)
                .build();

        Assert.assertNotNull(nameField.getColumn());
        Assert.assertTrue(nameField.getColumnName().startsWith(SQLType.VARCHAR64.prefix()));

        Assert.assertNotNull(barField.getColumn());
        Assert.assertTrue(barField.getColumnName().startsWith(SQLType.VALUE.prefix()));
    }

    public void testAllocateColumnForArray() {
        ClassType bazType = ClassBuilder.newBuilder("Baz", null).build();
        ClassType barType = ClassBuilder.newBuilder("Bar", null).build();
        Field barsField = FieldBuilder
                .newBuilder("bars", null, bazType, TypeUtil.getArrayType(barType))
                .build();
        Assert.assertNotNull(barsField.getColumn());
        Assert.assertTrue(barsField.getColumnName().startsWith(SQLType.MULTI_REFERENCE.prefix()));
    }

    public void testIsAssignable() {
        ClassType type1 = ClassBuilder.newBuilder("Foo", null).build();
        ClassType type2 = ClassBuilder.newBuilder("Foo", null).superType(type1).build();
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

}