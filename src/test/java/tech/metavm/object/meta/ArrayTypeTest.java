package tech.metavm.object.meta;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;

public class ArrayTypeTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockRegistry.setUp(new MockIdProvider());
    }

    public void testIsAssignable() {
        ObjectType objectType = MockRegistry.getObjectType();
        ClassType fooType = MockRegistry.getClassType(Foo.class);
        Assert.assertTrue(objectType.isAssignableFrom(fooType));

        ArrayType objectArrayType = new ArrayType(objectType, true);
        ArrayType fooArrayType = new ArrayType(fooType, true);

        Assert.assertTrue(objectArrayType.isAssignableFrom(fooArrayType));
        Assert.assertTrue(objectType.isAssignableFrom(objectArrayType));
    }

}
