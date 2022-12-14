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
        typeFactory = new TypeFactory(MockRegistry::getType);
    }

    public void testAllocateColumn() {
        ClassType fooType = typeFactory.createClass("Foo", null);

        ClassType barType = typeFactory.createValueClass("Bar", null);

        Field nameField = new Field(
                "name",
                fooType,
                Access.GLOBAL,
                false,
                true,
                null,
                MockRegistry.getStringType(),
                false
        );

        Field barField = new Field(
                "bar",
                fooType,
                Access.GLOBAL,
                false,
                false,
                null,
                barType,
                false
        );

        Assert.assertNotNull(nameField.getColumn());
        Assert.assertTrue(nameField.getColumnName().startsWith(SQLType.VARCHAR64.prefix()));

        Assert.assertNotNull(barField.getColumn());
        Assert.assertTrue(barField.getColumnName().startsWith(SQLType.VALUE.prefix()));
    }

    public void testAllocateColumnForArray() {
        ClassType bazType = typeFactory.createClass("Baz", null);
        ClassType barType = typeFactory.createValueClass("Bar", null);

        Field barsField = new Field(
                "bars",
                bazType,
                Access.GLOBAL,
                false,
                false,
                null,
                TypeUtil.getArrayType(barType),
                false
        );

        Assert.assertNotNull(barsField.getColumn());
        Assert.assertTrue(barsField.getColumnName().startsWith(SQLType.MULTI_REFERENCE.prefix()));
    }

    public void testIsAssignable() {
        ClassType type1 = typeFactory.createClass("Foo");
        ClassType type2 = typeFactory.createClass("Foo", type1);
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