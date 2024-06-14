package org.metavm.task;//package org.metavm.job;
//
//import junit.framework.TestCase;
//import org.junit.Assert;
//import org.metavm.entity.EntityIdProvider;
//import org.metavm.object.instance.core.InstanceContext;
//import org.metavm.entity.InstanceContextFactory;
//import org.metavm.mocks.Foo;
//import org.metavm.object.instance.core.Instance;
//import org.metavm.object.meta.Field;
//import org.metavm.object.meta.MetadataState;
//import org.metavm.util.MockIdProvider;
//import org.metavm.util.MockRegistry;
//import org.metavm.util.TestUtils;

//public class FieldRemovalJobTest extends TestCase {

//    private InstanceContextFactory instanceContextFactory;

//    @Override
//    protected void setUp() throws Exception {
//        EntityIdProvider idProvider = new MockIdProvider();
//        MockRegistry.setUp(idProvider);
//        instanceContextFactory = TestUtils.getInstanceContextFactory(idProvider);
//    }

//    public void test() {
//        InstanceContext context1 = instanceContextFactory.newContext();
//        Instance instance = MockRegistry.getNewFooInstance();
//        context1.bind(instance);
//        context1.finish();
//
//        InstanceContext context2 = instanceContextFactory.newContext();
//
//        Field fooCodeField = MockRegistry.getField(Foo.class, "code");
////        fooCodeField.setState(MetadataState.DELETING);
//
//        FieldRemovalJob job = new FieldRemovalJob(fooCodeField);
//        boolean done = job.executeBatch(context2);
//        Assert.assertTrue(done);
//        Assert.assertFalse(context2.getEntityContext().containsModel(fooCodeField));
//    }

//}