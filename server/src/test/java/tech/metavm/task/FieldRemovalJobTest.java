package tech.metavm.task;//package tech.metavm.job;
//
//import junit.framework.TestCase;
//import org.junit.Assert;
//import tech.metavm.entity.EntityIdProvider;
//import tech.metavm.object.instance.core.InstanceContext;
//import tech.metavm.entity.InstanceContextFactory;
//import tech.metavm.mocks.Foo;
//import tech.metavm.object.instance.core.Instance;
//import tech.metavm.object.meta.Field;
//import tech.metavm.object.meta.MetadataState;
//import tech.metavm.util.MockIdProvider;
//import tech.metavm.util.MockRegistry;
//import tech.metavm.util.TestUtils;

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