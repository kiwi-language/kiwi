package tech.metavm.flow;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.*;
import tech.metavm.task.TaskManager;
import tech.metavm.object.instance.*;
import tech.metavm.object.instance.log.InstanceLogServiceImpl;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.object.type.TypeManager;
import tech.metavm.util.*;

import java.util.List;

public class FlowManagerTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(FlowManagerTest.class);

    private FlowManager flowManager;

    private FlowExecutionService flowExecutionService;

    private InstanceContextFactory instanceContextFactory;

    @Override
    protected void setUp() throws Exception {
        EntityIdProvider idProvider = new MockIdProvider();
        MemInstanceStore instanceStore = new MemInstanceStore();
        MockRegistry.setUp(idProvider, instanceStore);

        instanceContextFactory = new InstanceContextFactory(instanceStore)
                .setIdService(idProvider)
                .setDefaultAsyncProcessing(false);
        InstanceContextFactory.setStdContext(MockRegistry.getInstanceContext());

        InstanceSearchService instanceSearchService = new MemInstanceSearchService();
        instanceContextFactory.setPlugins(List.of(
                        new CheckConstraintPlugin(),
                        new IndexConstraintPlugin(new MemIndexEntryMapper()),
                        new ChangeLogPlugin(new InstanceLogServiceImpl(
                                instanceSearchService,
                                instanceContextFactory,
                                instanceStore,
                                new MockTransactionOperations()))
        ));

        EntityQueryService entityQueryService =
                new EntityQueryService(new InstanceQueryService(instanceSearchService));

        TaskManager jobManager = new TaskManager(instanceContextFactory, new MockTransactionOperations());

        TypeManager typeManager =
                new TypeManager(instanceContextFactory, entityQueryService, jobManager,  null);
        flowManager = new FlowManager(instanceContextFactory);
        flowManager.setTypeManager(typeManager);
        flowExecutionService = new FlowExecutionService(instanceContextFactory);
    }


}