package tech.metavm.object.instance.core;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.util.MockUtils;

public class InstanceCopierTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void test() {
        var fooTypes = MockUtils.createFooTypes(true);
        var foo = MockUtils.createFoo(fooTypes, true);
        var fooCopy = (ClassInstance) foo.accept(new InstanceCopier(foo));
        Assert.assertNull(fooCopy.getId());
        Assert.assertEquals(foo.getField(fooTypes.fooNameField()), fooCopy.getField(fooTypes.fooNameField()));
    }

}