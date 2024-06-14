package org.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.entity.StandardTypes;

public class ArrayTypeTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void testIsAssignable() {
        var anyType = StandardTypes.getAnyType();
        var fooType = ClassTypeBuilder.newBuilder("Foo", "Foo").build().getType();
        Assert.assertTrue(anyType.isAssignableFrom(fooType));

        var objectArrayType = new ArrayType(anyType, ArrayKind.READ_WRITE);
        var fooArrayType = new ArrayType(fooType, ArrayKind.READ_WRITE);

        Assert.assertFalse(objectArrayType.isAssignableFrom(fooArrayType));
        Assert.assertTrue(anyType.isAssignableFrom(objectArrayType));

        var fooChildArrayType = new ArrayType(fooType, ArrayKind.CHILD);
        Assert.assertTrue(fooArrayType.isAssignableFrom(fooChildArrayType));
        Assert.assertFalse(fooChildArrayType.isAssignableFrom(fooArrayType));
    }

}
