package tech.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.entity.StandardTypes;

import java.util.Map;

public class ArrayTypeTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void testIsAssignable() {
        var anyType = StandardTypes.getAnyType();
        var fooType = ClassTypeBuilder.newBuilder("Foo", "Foo").build();
        Assert.assertTrue(anyType.isAssignableFrom(fooType, Map.of()));

        var objectArrayType = new ArrayType(null, anyType, ArrayKind.READ_WRITE);
        var fooArrayType = new ArrayType(null, fooType, ArrayKind.READ_WRITE);

        Assert.assertFalse(objectArrayType.isAssignableFrom(fooArrayType, Map.of()));
        Assert.assertTrue(anyType.isAssignableFrom(objectArrayType, Map.of()));

        var fooChildArrayType = new ArrayType(null, fooType, ArrayKind.CHILD);
        Assert.assertTrue(fooArrayType.isAssignableFrom(fooChildArrayType, Map.of()));
        Assert.assertFalse(fooChildArrayType.isAssignableFrom(fooArrayType, Map.of()));
    }

}
