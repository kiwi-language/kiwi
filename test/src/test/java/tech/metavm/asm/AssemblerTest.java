package tech.metavm.asm;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.asm.Assembler.AsmPrimitiveKind;
import tech.metavm.asm.Assembler.AsmType;
import tech.metavm.asm.Assembler.ClassAsmType;
import tech.metavm.asm.Assembler.PrimitiveAsmType;
import tech.metavm.entity.EntityQueryService;
import tech.metavm.flow.FlowExecutionService;
import tech.metavm.flow.FlowManager;
import tech.metavm.flow.FlowSavingContext;
import tech.metavm.object.instance.InstanceManager;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.instance.core.TmpId;
import tech.metavm.object.type.TypeManager;
import tech.metavm.object.type.rest.dto.BatchSaveRequest;
import tech.metavm.task.TaskManager;
import tech.metavm.util.BootstrapUtils;
import tech.metavm.util.MockTransactionOperations;
import tech.metavm.util.TestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class AssemblerTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(AssemblerTest.class);

    public void testParentChild() {
        final var source = "/Users/leen/workspace/object/test/src/test/resources/asm/ParentChild.masm";
        assemble(List.of(source));
    }

    public void testMyList() {
        final var source = "/Users/leen/workspace/object/test/src/test/resources/asm/List.masm";
//        assemble(List.of(source));
        deploy(List.of(source));
    }

    public void testShopping() {
        final var source = "/Users/leen/workspace/object/test/src/test/resources/asm/Shopping.masm";
        deploy(List.of(source));
    }

    public void testLivingBeing() {
        final var source = "/Users/leen/workspace/object/test/src/test/resources/asm/LivingBeing.masm";
        deploy(List.of(source));
    }

    public void testUtils() {
        final var source = "/Users/leen/workspace/object/test/src/test/resources/asm/Utils.masm";
        deploy(List.of(source));
    }

    private void assemble(List<String> sources) {
        var stdTypeIds = new HashMap<AsmType, String>();
        for (AsmPrimitiveKind primitiveKind : AsmPrimitiveKind.values()) {
            stdTypeIds.put(new PrimitiveAsmType(primitiveKind), TmpId.randomString());
        }
        stdTypeIds.put(Assembler.AnyAsmType.INSTANCE, TmpId.randomString());
        stdTypeIds.put(Assembler.NeverAsmType.INSTANCE, TmpId.randomString());
        stdTypeIds.put(new Assembler.UnionAsmType(Set.of(Assembler.AnyAsmType.INSTANCE, new Assembler.PrimitiveAsmType(Assembler.AsmPrimitiveKind.NULL))),
                TmpId.randomString());
        stdTypeIds.put(ClassAsmType.create("ChildList"), TmpId.randomString());
        stdTypeIds.put(ClassAsmType.create("List"), TmpId.randomString());
        stdTypeIds.put(ClassAsmType.create("ReadWriteList"), TmpId.randomString());
        stdTypeIds.put(ClassAsmType.create("Enum"), TmpId.randomString());
        stdTypeIds.put(ClassAsmType.create("RuntimeException"), TmpId.randomString());
        stdTypeIds.put(ClassAsmType.create("Iterable"), TmpId.randomString());
        stdTypeIds.put(ClassAsmType.create("Iterator"), TmpId.randomString());
        stdTypeIds.put(ClassAsmType.create("Predicate"), TmpId.randomString());
        var assembler = new Assembler(stdTypeIds);
        assemble(sources, assembler);
    }

    private BatchSaveRequest assemble(List<String> sources, Assembler assembler) {
        assembler.assemble(sources);
        var request = new BatchSaveRequest(assembler.getAllTypes(), List.of(), assembler.getParameterizedFlows(), false);
                TestUtils.writeJson("/Users/leen/workspace/object/test.json", request);
        return request;
    }

    private void deploy(List<String> sources) {
        var bootResult = BootstrapUtils.bootstrap();
        var instanceQueryService = new InstanceQueryService(bootResult.instanceSearchService());
        var typeManager = new TypeManager(
                bootResult.entityContextFactory(),
                new EntityQueryService(instanceQueryService),
                new TaskManager(bootResult.entityContextFactory(), new MockTransactionOperations()),
                new MockTransactionOperations()
        );
        var instanceManager = new InstanceManager(bootResult.entityContextFactory(),
                bootResult.instanceStore(), instanceQueryService);
        typeManager.setInstanceManager(instanceManager);
        var flowManager = new FlowManager(bootResult.entityContextFactory());
        flowManager.setTypeManager(typeManager);
        typeManager.setFlowManager(flowManager);
        var flowExecutionService = new FlowExecutionService(bootResult.entityContextFactory());
        typeManager.setFlowExecutionService(flowExecutionService);
        FlowSavingContext.initConfig();
        var assembler = AssemblerFactory.createWithStandardTypes();
        var request = assemble(sources, assembler);
//        DebugEnv.DEBUG_ON = true;
        TestUtils.doInTransaction(() -> typeManager.batchSave(request));
    }

}