package tech.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.mocks.Foo;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;

public class ArrayTypeTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockRegistry.setUp(new MockIdProvider());
    }

    public void testIsAssignable() {
        AnyType anyType = MockRegistry.getObjectType();
        ClassType fooType = MockRegistry.getClassType(Foo.class);
        Assert.assertTrue(anyType.isAssignableFrom(fooType));

        ArrayType objectArrayType = new ArrayType(null, anyType, ArrayKind.READ_WRITE);
        ArrayType fooArrayType = new ArrayType(null, fooType, ArrayKind.READ_WRITE);

        Assert.assertTrue(objectArrayType.isAssignableFrom(fooArrayType));
        Assert.assertTrue(anyType.isAssignableFrom(objectArrayType));
    }

}
