package tech.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;

public class ClassTypeFactoryTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockRegistry.setUp(new MockIdProvider());
    }

    public void testCreateClass() {
        ClassType type = ClassTypeBuilder.newBuilder("Foo", "Foo")
                .superClass(MockRegistry.getEntityType()).build();
        Assert.assertNotNull(type.getDeclaredFields());
        Assert.assertNotNull(type.getDeclaredConstraints());
        Assert.assertNotNull(type.getDeclaredMethods());
    }

}