package tech.metavm.asm;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.EntityQueryService;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.flow.FlowExecutionService;
import tech.metavm.flow.FlowManager;
import tech.metavm.flow.FlowSavingContext;
import tech.metavm.object.instance.InstanceManager;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.type.TypeManager;
import tech.metavm.object.type.rest.dto.BatchSaveRequest;
import tech.metavm.task.TaskManager;
import tech.metavm.util.BootstrapUtils;
import tech.metavm.util.MockTransactionOperations;
import tech.metavm.util.TestUtils;

import java.util.List;

public class AssemblerTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(AssemblerTest.class);

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void testParentChild() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/ParentChild.masm");
}

    public void testMyList() {
//        assemble(List.of(source));
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/List.masm");
    }

    public void testShopping() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/Shopping.masm");
    }

    public void testLivingBeing() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/LivingBeing.masm");
    }

    public void testUtils() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/Utils.masm");
    }

    public void testGenericOverloading() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/GenericOverloading.masm");
    }

    public void testLambda() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/Lambda.masm");
    }

    private void assemble(String source) {
        var assembler = AssemblerFactory.createWithStandardTypes();
        assemble(List.of(source), assembler);
    }

    private BatchSaveRequest assemble(List<String> sources, Assembler assembler) {
        assembler.assemble(sources);
        var request = new BatchSaveRequest(assembler.getAllTypeDefs(), List.of(), true);
        TestUtils.writeJson("/Users/leen/workspace/object/test.json", request);
        return request;
    }

    private void deploy(String source) {
        var bootResult = BootstrapUtils.bootstrap();
        var instanceQueryService = new InstanceQueryService(bootResult.instanceSearchService());
        var typeManager = new TypeManager(
                bootResult.entityContextFactory(),
                new EntityQueryService(instanceQueryService),
                new TaskManager(bootResult.entityContextFactory(), new MockTransactionOperations())
        );
        var instanceManager = new InstanceManager(bootResult.entityContextFactory(),
                bootResult.instanceStore(), instanceQueryService);
        typeManager.setInstanceManager(instanceManager);
        var flowManager = new FlowManager(bootResult.entityContextFactory(), new MockTransactionOperations());
        flowManager.setTypeManager(typeManager);
        typeManager.setFlowManager(flowManager);
        var flowExecutionService = new FlowExecutionService(bootResult.entityContextFactory());
        typeManager.setFlowExecutionService(flowExecutionService);
        FlowSavingContext.initConfig();
        var assembler = AssemblerFactory.createWithStandardTypes();
        var request = assemble(List.of(source), assembler);
//        DebugEnv.DEBUG_ON = true;
        TestUtils.doInTransaction(() -> typeManager.batchSave(request));
    }

}