package tech.metavm.object.instance;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.EntityQueryService;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.flow.FlowExecutionService;
import tech.metavm.flow.FlowManager;
import tech.metavm.flow.rest.FlowExecutionRequest;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceFieldValue;
import tech.metavm.object.instance.rest.PrimitiveFieldValue;
import tech.metavm.object.instance.rest.ReferenceFieldValue;
import tech.metavm.object.type.TypeManager;
import tech.metavm.task.TaskManager;
import tech.metavm.util.*;

import java.util.List;

public class ShoppingTest extends TestCase {

    private TypeManager typeManager;
    private FlowManager flowManager;
    private InstanceManager instanceManager;
    private FlowExecutionService flowExecutionService;

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
        var bootResult = BootstrapUtils.bootstrap();
        var instanceStore = bootResult.instanceStore();
        var entityContextFactory = bootResult.entityContextFactory();
        var transactionOptions = new MockTransactionOperations();
        var instanceQueryService = new InstanceQueryService(bootResult.instanceSearchService());
        typeManager = new TypeManager(entityContextFactory,
                new EntityQueryService(instanceQueryService),
                new TaskManager(entityContextFactory, transactionOptions),
                transactionOptions);
        flowManager = new FlowManager(entityContextFactory, new MockTransactionOperations());
        flowManager.setTypeManager(typeManager);
        typeManager.setFlowManager(flowManager);
        instanceManager = new InstanceManager(entityContextFactory, instanceStore, instanceQueryService);
        typeManager.setInstanceManager(instanceManager);
        flowExecutionService = new FlowExecutionService(entityContextFactory);
        typeManager.setFlowExecutionService(flowExecutionService);
    }

    @Override
    protected void tearDown() {
        typeManager = null;
        flowManager = null;
        instanceManager = null;
        flowExecutionService = null;
    }

    public void testDecAmount() {
        var shoppingTypeIds = MockUtils.createShoppingTypes(typeManager);
        var productDTO = MockUtils.createProductDTO(shoppingTypeIds);
        var productId = TestUtils.doInTransaction(() -> instanceManager.create(productDTO));
        var loadedProductDTO = instanceManager.get(productId, 1).instance();
        var firstSkuDTO = loadedProductDTO.getFieldValue(shoppingTypeIds.productSkuListFieldId()).underlyingInstance()
                .getElement(0).underlyingInstance();
        long originalAmount = (long) ((PrimitiveFieldValue) firstSkuDTO.getFieldValue(shoppingTypeIds.skuAmountFieldId())).getValue();
        TestUtils.doInTransactionWithoutResult(
                () -> flowExecutionService.execute(new FlowExecutionRequest(
                        shoppingTypeIds.skuDecAmountMethodId(),
                        firstSkuDTO.id(),
                        List.of(PrimitiveFieldValue.createLong(1L)))
                )
        );
        var updatedFirstSkuDTO = instanceManager.get(firstSkuDTO.id(), 1).instance();
        long newAmount = (long) ((PrimitiveFieldValue) updatedFirstSkuDTO.getFieldValue(shoppingTypeIds.skuAmountFieldId())).getValue();
        assertEquals(originalAmount - 1, newAmount);
        try {
            TestUtils.doInTransaction(
                    () -> flowExecutionService.execute(new FlowExecutionRequest(
                            shoppingTypeIds.skuDecAmountMethodId(),
                            firstSkuDTO.id(),
                            List.of(PrimitiveFieldValue.createLong(originalAmount)))
                    )
            );
            fail("Should fail when amount is not enough");
        } catch (FlowExecutionException e) {
            Assert.assertEquals("库存不足", e.getMessage());
        }
    }

    public void testBuy() {
        var shoppingTypeIds = MockUtils.createShoppingTypes(typeManager);
        var productDTO = MockUtils.createProductDTO(instanceManager, shoppingTypeIds);
        var couponsDTOs = MockUtils.createCouponDTOs(instanceManager, shoppingTypeIds);
        var firstSkuDTO = productDTO.getFieldValue(shoppingTypeIds.productSkuListFieldId()).underlyingInstance()
                .getElement(0).underlyingInstance();
        var arguments = List.of(PrimitiveFieldValue.createLong(1L),
                InstanceFieldValue.of(
                        InstanceDTO.createListInstance(
                                shoppingTypeIds.couponListTypeId(),
                                false,
                                NncUtils.map(couponsDTOs, ReferenceFieldValue::create)
                        ))
        );
        var orderDTO = TestUtils.doInTransaction(
                () -> flowExecutionService.execute(
                        new FlowExecutionRequest(shoppingTypeIds.skuBuyMethodId(), firstSkuDTO.id(), arguments)
                )
        );
        Assert.assertNotNull(orderDTO);
        var order = instanceManager.get(orderDTO.id(), 1).instance();
        var amount = (long) ((PrimitiveFieldValue) order.getFieldValue(shoppingTypeIds.orderAmountFieldId())).getValue();
        Assert.assertEquals(1L, amount);
        var orderPrice = (double) ((PrimitiveFieldValue) order.getFieldValue(shoppingTypeIds.orderPriceFieldId())).getValue();
        Assert.assertEquals(70.0, orderPrice, 0.0001);

        couponsDTOs = instanceManager.batchGet(NncUtils.map(couponsDTOs, InstanceDTO::id), 1).instances();
        for (var couponDTO : couponsDTOs) {
            var coupon = instanceManager.get(couponDTO.id(), 1).instance();
            var state = coupon.getFieldValue(shoppingTypeIds.couponStateFieldId()).referenceId();
            Assert.assertEquals(shoppingTypeIds.couponUsedStateId(), state);
        }
        var reloadedFirstSkuDTO = instanceManager.get(firstSkuDTO.id(), 1).instance();
        var originalAmount = (long) ((PrimitiveFieldValue) firstSkuDTO.getFieldValue(shoppingTypeIds.skuAmountFieldId())).getValue();
        var skuAmount = (long) ((PrimitiveFieldValue) reloadedFirstSkuDTO.getFieldValue(shoppingTypeIds.skuAmountFieldId())).getValue();
        Assert.assertEquals(originalAmount - 1, skuAmount);
    }

}
