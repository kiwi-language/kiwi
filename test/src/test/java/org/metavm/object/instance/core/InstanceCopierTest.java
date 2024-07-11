//package org.metavm.object.instance.core;
//
//import junit.framework.TestCase;
//import org.junit.Assert;
//import org.metavm.entity.MockStandardTypesInitializer;
//import org.metavm.util.MockUtils;
//
//public class InstanceCopierTest extends TestCase {
//
//    @Override
//    protected void setUp() throws Exception {
//        MockStandardTypesInitializer.init();
//    }
//
//    public void test() {
//        var fooTypes = MockUtils.createFooTypes(true);
//        var foo = MockUtils.createFoo(fooTypes, true);
//        var fooCopy = (ClassInstance) foo.accept(new InstanceCopier(foo));
//        Assert.assertNull(fooCopy.tryGetId());
//        Assert.assertEquals(foo.getField(fooTypes.fooNameField()), fooCopy.getField(fooTypes.fooNameField()));
//    }
//
//}