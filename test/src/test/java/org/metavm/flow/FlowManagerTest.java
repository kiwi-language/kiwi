package org.metavm.flow;

import junit.framework.TestCase;
import org.metavm.entity.EntityQueryService;
import org.metavm.flow.rest.*;
import org.metavm.object.instance.InstanceQueryService;
import org.metavm.object.instance.core.TmpId;
import org.metavm.object.type.BeanManager;
import org.metavm.object.type.TypeManager;
import org.metavm.object.type.rest.dto.ClassTypeDTOBuilder;
import org.metavm.object.type.rest.dto.FieldDTOBuilder;
import org.metavm.task.TaskManager;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FlowManagerTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(FlowManagerTest.class);

    private FlowManager flowManager;

    private TypeManager typeManager;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        var entityContextFactory = bootResult.entityContextFactory();
        var instanceSearchService = bootResult.instanceSearchService();
        var entityQueryService =
                new EntityQueryService(new InstanceQueryService(instanceSearchService));
        var jobManager = new TaskManager(entityContextFactory, new MockTransactionOperations());
        typeManager =
                new TypeManager(entityContextFactory, entityQueryService, jobManager, new BeanManager());
        flowManager = new FlowManager(entityContextFactory, new MockTransactionOperations());
        flowManager.setTypeManager(typeManager);
        FlowSavingContext.initConfig();
        ContextUtil.setAppId(TestConstants.APP_ID);
    }

    @Override
    protected void tearDown() {
        flowManager = null;
        typeManager = null;
        FlowSavingContext.clearConfig();
    }

    public void testDecreaseQuantity() {
        var type = TestUtils.doInTransaction(() -> typeManager.saveType(ClassTypeDTOBuilder.newBuilder("Product")
                .tmpId(NncUtils.randomNonNegative())
                .addField(FieldDTOBuilder.newBuilder("title", "string").build())
                .addField(FieldDTOBuilder.newBuilder("price", "double").build())
                .addField(FieldDTOBuilder.newBuilder("quantity", "long").build())
                .build()));

        var flow = TestUtils.doInTransaction(() -> flowManager.save(MethodDTOBuilder.newBuilder(type.id(), "decreaseQuantity")
                .tmpId(NncUtils.randomNonNegative())
                .parameters(List.of(ParameterDTO.create(null, "quantity", "long")))
                .returnType("void")
                .skipRootScope(true)
                .build()));

        var inputNode = flow.getRootScope().getNodeByIndex(1);

        var branchNode = TestUtils.doInTransaction(() -> flowManager.createBranchNode(
                new NodeDTO(
                        null,
                        flow.getStringId(),
                        "Branch",
                        null,
                        NodeKind.BRANCH.code(),
                        inputNode.getStringId(),
                        null,
                        new BranchNodeParam(false,
                                List.of(
                                        new BranchDTO(
                                                null, 0L, null,
                                                ValueDTOFactory.createExpression("true"),
                                                new ScopeDTO(null, List.of()), false, false
                                        ),
                                        new BranchDTO(
                                                null, 10000L, null,
                                                ValueDTOFactory.createExpression("true"),
                                                new ScopeDTO(null, List.of()), true, false
                                        )
                                )
                        ),
                        null,
                        flow.getRootScope().getStringId(),
                        null
                )
        )).get(0);

        var branch = ((BranchNodeParam) branchNode.param()).branches().get(0);

        TestUtils.doInTransaction(() -> flowManager.updateBranch(
                new BranchDTO(
                        branch.id(),
                        branch.index(),
                        branchNode.id(),
                        ValueDTOFactory.createExpression("input.quantity > self.quantity"),
                        new ScopeDTO(null, List.of()),
                        false,
                        false
                )
        ));

        TestUtils.doInTransaction(() -> flowManager.saveNode(
                new NodeDTO(
                        TmpId.random().toString(),
                        flow.getStringId(),
                        "Error",
                        null,
                        NodeKind.EXCEPTION.code(),
                        null,
                        null,
                        new RaiseNodeParam(
                                RaiseParameterKind.MESSAGE.getCode(),
                                ValueDTOFactory.createConstant("Quantity not enough"),
                                null
                        ),
                        null,
                        branch.scope().id(),
                        null
                )
        ));

    }


}