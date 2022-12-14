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
        TypeFactory typeFactory = new TypeFactory(MockRegistry::getType);
        ClassType type = typeFactory.createClass("Foo", MockRegistry.getEntityType());
        Assert.assertNotNull(type.getDeclaredFields());
        Assert.assertNotNull(type.getDeclaredConstraints());
        Assert.assertNotNull(type.getDeclaredFlows());
    }

}