package tech.metavm.object.meta;

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
        ClassType type = ClassBuilder.newBuilder("Foo", "Foo")
                .superType(MockRegistry.getEntityType()).build();
        Assert.assertNotNull(type.getDeclaredFields());
        Assert.assertNotNull(type.getDeclaredConstraints());
        Assert.assertNotNull(type.getDeclaredFlows());
    }

}