package tech.metavm.flow;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.*;
import tech.metavm.event.MockEventQueue;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.instance.MemInstanceSearchService;
import tech.metavm.object.instance.MockInstanceLogService;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.object.type.IndexRepository;
import tech.metavm.object.type.TypeManager;
import tech.metavm.object.type.mocks.MockIndexRepository;
import tech.metavm.task.TaskManager;
import tech.metavm.util.BootstrapUtils;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;
import tech.metavm.util.MockTransactionOperations;

public class FlowManagerTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(FlowManagerTest.class);

    private FlowManager flowManager;

    private FlowExecutionService flowExecutionService;

    private InstanceContextFactory instanceContextFactory;

    private IndexRepository indexRepository;

    @Override
    protected void setUp() throws Exception {
        indexRepository = new MockIndexRepository();
        EntityIdProvider idProvider = new MockIdProvider();
        MemInstanceStore instanceStore = new MemInstanceStore();
        var indexEntryMapper = new MemIndexEntryMapper();

        instanceContextFactory = new InstanceContextFactory(instanceStore, new MockEventQueue())
                .setIdService(idProvider);
        var entityContextFactory = new EntityContextFactory(instanceContextFactory, indexEntryMapper);
        InstanceContextFactory.setDefContext(MockRegistry.getDefContext());
        BootstrapUtils.bootstrap(entityContextFactory);

        InstanceSearchService instanceSearchService = new MemInstanceSearchService();

        EntityQueryService entityQueryService =
                new EntityQueryService(new InstanceQueryService(instanceSearchService));

        TaskManager jobManager = new TaskManager(entityContextFactory, new MockTransactionOperations());

        TypeManager typeManager =
                new TypeManager(entityContextFactory, entityQueryService, jobManager,  null);
        flowManager = new FlowManager(entityContextFactory);
        flowManager.setTypeManager(typeManager);
        flowExecutionService = new FlowExecutionService(entityContextFactory);
    }

    public void test() {

    }


}