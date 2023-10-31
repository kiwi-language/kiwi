package tech.metavm.flow;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.dto.Page;
import tech.metavm.dto.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.flow.rest.*;
import tech.metavm.task.TaskManager;
import tech.metavm.mocks.Coupon;
import tech.metavm.mocks.CouponState;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.*;
import tech.metavm.object.instance.log.InstanceLogServiceImpl;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.TypeManager;
import tech.metavm.user.RoleRT;
import tech.metavm.user.UserRT;
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
                                instanceStore
                        ))
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

    public void test() {
        ClassType userType = MockRegistry.getClassType(UserRT.class);
        FlowDTO flowDTO = FlowDTO.create("Flow1", userType.getIdRequired());
        long flowId = flowManager.save(flowDTO).getIdRequired();
        FlowDTO loadedFlowDTO = flowManager.get(new GetFlowRequest(flowId, true)).flow();
        Assert.assertEquals(flowId, (long) loadedFlowDTO.id());
        Assert.assertEquals(3, loadedFlowDTO.rootScope().nodes().size());

        ScopeDTO rootScope = loadedFlowDTO.rootScope();
        long selfNodeId = rootScope.nodes().get(1).id();

        Field userNameField = MockRegistry.getField(UserRT.class, "name");

        NodeDTO updateNode = new NodeDTO(
                null, null, flowId, "UpdateUser", NodeKind.UPDATE_OBJECT.code(),
                RefDTO.ofId(selfNodeId), null,
                new UpdateObjectParamDTO(
                    ValueDTO.refValue("当前记录"),
                    List.of(
                            new UpdateFieldDTO(
                                    RefDTO.ofId(userNameField.getId()),
                                    UpdateOp.SET.code(),
                                    ValueDTO.constValue(
                                            InstanceUtils.stringInstance("lyq").toFieldValueDTO()
                                    )
                            )
                    )
                ),
                null,
                rootScope.id()
        );

        flowManager.createNode(updateNode);

        loadedFlowDTO = flowManager.get(new GetFlowRequest(flowId, true)).flow();
        Assert.assertEquals(4, loadedFlowDTO.rootScope().nodes().size());

        TestUtils.logJSON(LOGGER, loadedFlowDTO);
    }

    public void testCoupon() {
        ClassType couponType = MockRegistry.getClassType(Coupon.class);
        FlowDTO flowDTO = FlowDTO.create("use", couponType.getIdRequired());

        long flowId = flowManager.save(flowDTO).getIdRequired();
        flowDTO = flowManager.get(new GetFlowRequest(flowId, true)).flow();

        Field couponStateField = MockRegistry.getField(Coupon.class, "state");

        Instance usedStateInstance = MockRegistry.getInstance(CouponState.USED);

        NodeDTO selfNode = flowDTO.rootScope().nodes().get(1);
        NodeDTO updateNode = new NodeDTO(
                null, null, flowId, "UpdateState", NodeKind.UPDATE_OBJECT.code(),
                RefDTO.ofId(selfNode.id()), null,
                new UpdateObjectParamDTO(
                        ValueDTO.refValue("当前记录"),
                        List.of(
                                new UpdateFieldDTO(
                                        RefDTO.ofId(couponStateField.getId()),
                                        UpdateOp.SET.code(),
                                        ValueDTO.constValue(
                                                usedStateInstance.toFieldValueDTO()
                                        )
                                )
                        )
                ),
                null,
                flowDTO.rootScope().id()
        );

        flowManager.createNode(updateNode);

        ClassInstance couponInst = MockRegistry.getCouponInstance();
        FlowExecutionRequest request = new FlowExecutionRequest(flowId, couponInst.getIdRequired(), List.of());
        flowExecutionService.execute(request);

        IEntityContext context = instanceContextFactory.newContext().getEntityContext();
        Coupon coupon = context.getEntity(Coupon.class, couponInst.getIdRequired());
        Assert.assertEquals(CouponState.USED, coupon.getState());
    }

    public void testList() {
        ClassType userType = MockRegistry.getClassType(UserRT.class);
        FlowDTO flowDTO = new FlowDTO(null, null, "Flow1", null, false,
                false, false,
                RefDTO.ofId(userType.getId()),  null, null,
                null, null,null,
                 null, null, List.of(),
                null, List.of());
        long flowId = flowManager.save(flowDTO).getIdRequired();
        Page<FlowSummaryDTO> dataPage = flowManager.list(userType.getIdRequired(), 1, 20, null);
        Assert.assertEquals(1, dataPage.total());
        FlowSummaryDTO flowSummaryDTO = dataPage.data().get(0);
        Assert.assertEquals(Long.valueOf(flowId), flowSummaryDTO.id());

        ClassType roleType = MockRegistry.getClassType(RoleRT.class);
        Page<FlowSummaryDTO> dataPage2 = flowManager.list(roleType.getIdRequired(), 1, 20, null);
        Assert.assertEquals(0, dataPage2.total());
    }

    public void testRemove() {
        ClassType fooType = MockRegistry.getClassType(Foo.class);
        FlowDTO flowDTO = FlowDTO.create("test", fooType.getIdRequired());
        long flowId = flowManager.save(flowDTO).getIdRequired();
        flowManager.delete(flowId);
    }

}