package tech.metavm.entity;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.mocks.EntityFoo;
import tech.metavm.event.MockEventQueue;
import tech.metavm.object.instance.MockInstanceLogService;
import tech.metavm.object.instance.core.InstanceContext;
import tech.metavm.object.type.BootIdProvider;
import tech.metavm.object.type.MemAllocatorStore;
import tech.metavm.object.type.MemColumnStore;
import tech.metavm.object.type.StdAllocators;
import tech.metavm.util.Constants;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.NncUtils;
import tech.metavm.util.TestConstants;

import java.util.concurrent.Executors;

import static tech.metavm.util.Constants.ROOT_APP_ID;

public class EntityContextTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(EntityContextTest.class);

    private IEntityContext entityContext;


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        var instanceStore = new MemInstanceStore();
        var eventQueue = new MockEventQueue();
        var columnStore = new MemColumnStore();
        var idService = new MockIdProvider();
        var indexEntryMapper = new MemIndexEntryMapper();
        var instanceLogService = new MockInstanceLogService();

        var stdAllocators = new StdAllocators(new MemAllocatorStore());

        var stdContext = (InstanceContext) EntityContextBuilder.newBuilder(
                        instanceStore, Executors.newSingleThreadExecutor(),
                        new BootIdProvider(stdAllocators))
                .appId(Constants.ROOT_APP_ID)
                .buildInstanceContext();

        var instanceContextFactory = new InstanceContextFactory(instanceStore, eventQueue);
        var entityContextFactory = new EntityContextFactory(instanceContextFactory, indexEntryMapper);
        entityContextFactory.setInstanceLogService(instanceLogService);
        var defContext = new DefContext(stdAllocators::getId, stdContext, columnStore);
//        stdContext.setDefContext(defContext);
//        stdContext.setEntityContext(defContext);

        instanceContextFactory.setIdService(idService);
        InstanceContextFactory.setDefContext(defContext);

        ModelDefRegistry.setDefContext(defContext);
        for (Class<?> entityClass : EntityUtils.getModelClasses()) {
            if (!ReadonlyArray.class.isAssignableFrom(entityClass) && !entityClass.isAnonymousClass())
                defContext.getDef(entityClass);
        }
        defContext.flushAndWriteInstances();

        try (IEntityContext tempContext = entityContextFactory.newContext(ROOT_APP_ID)) {
            NncUtils.requireNonNull(defContext.getInstanceContext()).increaseVersionsForAll();
            defContext.finish();
            defContext.getIdentityMap().forEach((model, javaConstruct) ->
                    stdAllocators.putId(javaConstruct, defContext.getInstance(model).getPhysicalId())
            );
//            defContext.getInstanceMapping().forEach((javaConstruct, instance) ->
//                    stdAllocators.putId(javaConstruct, instance.getIdRequired())
//            );
            stdAllocators.save();
            columnStore.save();
            tempContext.finish();
        }

        entityContext = entityContextFactory.newContext(TestConstants.APP_ID);
    }

    public void test() {
        var foo = new EntityFoo("foo");
        entityContext.bind(foo);
        entityContext.finish();
    }


}