package tech.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.entity.StandardTypes;

public class ArrayTypeTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void testIsAssignable() {
        var anyType = StandardTypes.getAnyType();
        ClassType fooType = ClassTypeBuilder.newBuilder("Foo", "Foo").build();
        Assert.assertTrue(anyType.isAssignableFrom(fooType));

        ArrayType objectArrayType = new ArrayType(null, anyType, ArrayKind.READ_WRITE);
        ArrayType fooArrayType = new ArrayType(null, fooType, ArrayKind.READ_WRITE);

        Assert.assertFalse(objectArrayType.isAssignableFrom(fooArrayType));
        Assert.assertTrue(anyType.isAssignableFrom(objectArrayType));
    }

}
