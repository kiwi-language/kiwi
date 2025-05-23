package org.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.util.TestUtils;

public class ArrayTypeTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        TestUtils.ensureStringKlassInitialized();
        MockStandardTypesInitializer.init();
    }

    public void testIsAssignable() {
        var anyType = Types.getAnyType();
        var fooType = TestUtils.newKlassBuilder("Foo", "Foo").build().getType();
        Assert.assertTrue(anyType.isAssignableFrom(fooType));

        var objectArrayType = new ArrayType(anyType, ArrayKind.DEFAULT);
        var fooArrayType = new ArrayType(fooType, ArrayKind.DEFAULT);

        Assert.assertFalse(objectArrayType.isAssignableFrom(fooArrayType));
        Assert.assertTrue(anyType.isAssignableFrom(objectArrayType));

        Assert.assertTrue(fooArrayType.isAssignableFrom(fooArrayType));
    }

}
