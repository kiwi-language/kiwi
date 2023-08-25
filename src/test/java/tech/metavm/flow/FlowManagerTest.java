package tech.metavm.flow;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.dto.Page;
import tech.metavm.entity.*;
import tech.metavm.flow.rest.*;
import tech.metavm.job.JobManager;
import tech.metavm.mocks.Coupon;
import tech.metavm.mocks.CouponState;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.*;
import tech.metavm.object.instance.log.InstanceLogServiceImpl;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.TypeManager;
import tech.metavm.object.meta.Field;
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

        JobManager jobManager = new JobManager(instanceContextFactory, new MockTransactionOperations());

        TypeManager typeManager =
                new TypeManager(instanceContextFactory, entityQueryService, jobManager,  null);
        flowManager = new FlowManager(instanceContextFactory, typeManager, entityQueryService);
        flowExecutionService = new FlowExecutionService(instanceContextFactory);
    }

    public void test() {
        ClassType userType = MockRegistry.getClassType(UserRT.class);
        FlowDTO flowDTO = FlowDTO.create("Flow1", userType.getId());
        long flowId = flowManager.create(flowDTO);
        FlowDTO loadedFlowDTO = flowManager.get(flowId);
        Assert.assertEquals(flowId, (long) loadedFlowDTO.id());
        Assert.assertEquals(3, loadedFlowDTO.rootScope().nodes().size());

        ScopeDTO rootScope = loadedFlowDTO.rootScope();
        long selfNodeId = rootScope.nodes().get(1).id();

        Field userNameField = MockRegistry.getField(UserRT.class, "name");

        NodeDTO updateNode = new NodeDTO(
                null, flowId, "UpdateUser", NodeKind.UPDATE_OBJECT.code(),
                selfNodeId, null,
                new UpdateObjectParamDTO(
                    ValueDTO.refValue("当前记录"),
                    List.of(
                            new UpdateFieldDTO(
                                    userNameField.getId(),
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

        loadedFlowDTO = flowManager.get(flowId);
        Assert.assertEquals(4, loadedFlowDTO.rootScope().nodes().size());

        TestUtils.logJSON(LOGGER, loadedFlowDTO);
    }

    public void testCoupon() {
        ClassType couponType = MockRegistry.getClassType(Coupon.class);
        FlowDTO flowDTO = FlowDTO.create("use", couponType.getId());

        long flowId = flowManager.create(flowDTO);
        flowDTO = flowManager.get(flowId);

        Field couponStateField = MockRegistry.getField(Coupon.class, "state");

        Instance usedStateInstance = MockRegistry.getInstance(CouponState.USED);

        NodeDTO selfNode = flowDTO.rootScope().nodes().get(1);
        NodeDTO updateNode = new NodeDTO(
                null, flowId, "UpdateState", NodeKind.UPDATE_OBJECT.code(),
                selfNode.id(), null,
                new UpdateObjectParamDTO(
                        ValueDTO.refValue("当前记录"),
                        List.of(
                                new UpdateFieldDTO(
                                        couponStateField.getId(),
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
        FlowExecutionRequest request = new FlowExecutionRequest(flowId, couponInst.getId(), List.of());
        flowExecutionService.execute(request);

        IEntityContext context = instanceContextFactory.newContext().getEntityContext();
        Coupon coupon = context.getEntity(Coupon.class, couponInst.getId());
        Assert.assertEquals(CouponState.USED, coupon.getState());
    }

    public void testList() {
        ClassType userType = MockRegistry.getClassType(UserRT.class);
        FlowDTO flowDTO = new FlowDTO(null, "Flow1", userType.getId(), null, null, 0L, 0L);
        long flowId = flowManager.create(flowDTO);
        Page<FlowSummaryDTO> dataPage = flowManager.list(userType.getId(), 1, 20, null);
        Assert.assertEquals(1, dataPage.total());
        FlowSummaryDTO flowSummaryDTO = dataPage.data().get(0);
        Assert.assertEquals(Long.valueOf(flowId), flowSummaryDTO.id());

        ClassType roleType = MockRegistry.getClassType(RoleRT.class);
        Page<FlowSummaryDTO> dataPage2 = flowManager.list(roleType.getId(), 1, 20, null);
        Assert.assertEquals(0, dataPage2.total());
    }

    public void testRemove() {
        ClassType fooType = MockRegistry.getClassType(Foo.class);
        FlowDTO flowDTO = FlowDTO.create("test", fooType.getId());
        long flowId = flowManager.create(flowDTO);
        flowManager.delete(flowId);
    }

}