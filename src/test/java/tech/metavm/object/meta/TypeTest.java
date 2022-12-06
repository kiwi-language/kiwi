package tech.metavm.object.meta;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.object.instance.SQLColumnType;

public class TypeTest extends TestCase {

    public void testAllocateColumn() {
        Type fooType = new Type(
                "Foo", StandardTypes.OBJECT, TypeCategory.CLASS
        );

        Type barType = new Type(
                "bar", StandardTypes.OBJECT, TypeCategory.VALUE
        );

        Field nameField = new Field(
                "name",
                fooType,
                Access.GLOBAL,
                false,
                true,
                null,
                StandardTypes.STRING,
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
        Assert.assertTrue(nameField.getColumnName().startsWith(SQLColumnType.VARCHAR64.prefix()));

        Assert.assertNotNull(barField.getColumn());
        Assert.assertTrue(barField.getColumnName().startsWith(SQLColumnType.OBJECT.prefix()));
    }

    public void testAllocateColumnForArray() {
        Type bazType = new Type(
                "Baz", StandardTypes.OBJECT, TypeCategory.CLASS
        );

        Type barType = new Type(
                "Bar", StandardTypes.OBJECT, TypeCategory.VALUE
        );

        Field barsField = new Field(
                "bars",
                bazType,
                Access.GLOBAL,
                false,
                false,
                null,
                barType.getArrayType(),
                false
        );

        Assert.assertNotNull(barsField.getColumn());
        Assert.assertTrue(barsField.getColumnName().startsWith(SQLColumnType.REFERENCE.prefix()));
    }

}