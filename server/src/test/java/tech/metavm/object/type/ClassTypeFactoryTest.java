package tech.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.util.MockUtils;

public class ClassTypeFactoryTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void testCreateClass() {
        var fooTypes = MockUtils.createFooTypes(true);
        var type = fooTypes.fooType();
        Assert.assertNotNull(type.getDeclaredFields());
        Assert.assertNotNull(type.getDeclaredConstraints());
        Assert.assertNotNull(type.getDeclaredMethods());
    }

}