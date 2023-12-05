package tech.metavm.task;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.entity.MemInstanceStore;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.type.Field;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;
import tech.metavm.util.TestConstants;
import tech.metavm.util.TestUtils;

public class ReferenceCleanupJobTest extends TestCase {

    @SuppressWarnings("FieldCanBeLocal")
    private MemInstanceStore instanceStore;
    private InstanceContextFactory instanceContextFactory;

    @Override
    protected void setUp() throws Exception {
        MockIdProvider idProvider = new MockIdProvider();
        instanceStore = new MemInstanceStore();
        MockRegistry.setUp(idProvider, instanceStore);
        instanceContextFactory = TestUtils.getInstanceContextFactory(idProvider, instanceStore);
    }

    public void test() {
        Field fooQuxField = MockRegistry.getField(Foo.class, "qux");

        ClassInstance foo = MockRegistry.getNewFooInstance();
        IInstanceContext context = newContext();
        context.bind(foo);
        context.finish();

        ClassInstance qux = foo.getClassInstance(fooQuxField);

        ReferenceCleanupTask job = new ReferenceCleanupTask(qux.getId(), qux.getType().getName(), qux.getTitle());
        context = newContext();
        context.getEntityContext().bind(job);
        context.remove(context.get(qux.getId()));
        context.getEntityContext().finish();

        context = newContext();
        IEntityContext entityContext = context.getEntityContext();
        job = entityContext.getEntity(ReferenceCleanupTask.class, job.getId());
        job.run(context);
        context.finish();

        context = newContext();
        foo = (ClassInstance) context.get(foo.getId());
        Assert.assertTrue(foo.getField(fooQuxField).isNull());
    }

    private IInstanceContext newContext() {
        return instanceContextFactory.newContext(TestConstants.APP_ID);
    }

}