package tech.metavm.flow;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.*;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.type.IndexRepository;
import tech.metavm.object.type.TypeManager;
import tech.metavm.object.type.mocks.MockIndexRepository;
import tech.metavm.task.TaskManager;
import tech.metavm.util.BootstrapUtils;
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
        var bootResult = BootstrapUtils.bootstrap();
        var entityContextFactory = bootResult.entityContextFactory();
        instanceContextFactory = bootResult.entityContextFactory().getInstanceContextFactory();
        var instanceSearchService =bootResult.instanceSearchService();
        var entityQueryService =
                new EntityQueryService(new InstanceQueryService(instanceSearchService));
        var jobManager = new TaskManager(entityContextFactory, new MockTransactionOperations());
        var typeManager =
                new TypeManager(entityContextFactory, entityQueryService, jobManager,  null);
        flowManager = new FlowManager(entityContextFactory);
        flowManager.setTypeManager(typeManager);
        flowExecutionService = new FlowExecutionService(entityContextFactory);
    }

    public void test() {

    }


}