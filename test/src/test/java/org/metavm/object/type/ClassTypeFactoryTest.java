package org.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.util.MockUtils;

public class ClassTypeFactoryTest extends TestCase {

    public void testCreateClass() {
        var fooTypes = MockUtils.createFooTypes(true);
        var type = fooTypes.fooType();
        Assert.assertNotNull(type.getDeclaredFields());
        Assert.assertNotNull(type.getDeclaredConstraints());
        Assert.assertNotNull(type.getDeclaredMethods());
    }

}