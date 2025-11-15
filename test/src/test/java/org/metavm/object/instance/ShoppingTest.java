package org.metavm.object.instance;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.EntityContextFactory;
import org.metavm.object.instance.core.ApiObject;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.TypeManager;
import org.metavm.util.*;

import java.util.List;
import java.util.Map;

public class ShoppingTest extends TestCase {

    private TypeManager typeManager;
    private EntityContextFactory entityContextFactory;
    private SchedulerAndWorker schedulerAndWorker;
    private ApiClient apiClient;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        var managers = TestUtils.createCommonManagers(bootResult);
        typeManager = managers.typeManager();
        schedulerAndWorker = bootResult.schedulerAndWorker();
        entityContextFactory = bootResult.entityContextFactory();
        apiClient = new ApiClient(new ApiService(entityContextFactory, bootResult.metaContextCache(),
                new InstanceQueryService(bootResult.instanceSearchService())));
    }

    @Override
    protected void tearDown() {
        typeManager = null;
        schedulerAndWorker = null;
        entityContextFactory = null;
        apiClient = null;
    }

    public void testCreateProduct() {
        MockUtils.assemble("kiwi/shopping.kiwi", typeManager, schedulerAndWorker);
        TestUtils.doInTransaction(() -> apiClient.saveInstance("Product", Map.of(
           "name", "shoes",
           "price", 100,
           "stock", 100
        )));
    }

    public void testReduceStock() {
        MockUtils.assemble("kiwi/shopping.kiwi", typeManager, schedulerAndWorker);
        var productId = createProduct();
        var product = getObject(productId);
        int origStock = product.getInt("stock");
        callMethod(productId, "reduceStock", List.of(1));
        var updatedProduct = getObject(productId);
        int newStock = updatedProduct.getInt("stock");
        assertEquals(origStock - 1, newStock);
        try {
            callMethod(productId, "reduceStock", List.of(origStock));
            fail("Should fail due to insufficient stock");
        } catch (BusinessException e) {
            Assert.assertEquals("Insufficient stock", e.getMessage());
        }
    }

    public void testBuy() {
        MockUtils.assemble("kiwi/shopping.kiwi", typeManager, schedulerAndWorker);
        var productId = createProduct();
        var product = getObject(productId);
        var couponId = createCoupon();
        var orderService = ApiNamedObject.of("orderService");
        var orderId = (Id) callMethod(orderService, "placeOrder", List.of(productId, 1, couponId));
        var order = getObject(orderId);
        Assert.assertEquals(1, order.getChildren("Item").getFirst().getInt("quantity"));
        Assert.assertEquals(90.0, order.getDouble("totalPrice"), 0.0001);
        var coupon = getObject(couponId);
        Assert.assertEquals(true, coupon.get("used"));
        var reloadedFirstSkuDTO = getObject(productId);
        var origStock = product.getInt("stock");
        var stock = reloadedFirstSkuDTO.getInt("stock");
        Assert.assertEquals(origStock - 1, stock);
    }

    private Id createProduct() {
        return saveInstance("Product", Map.of(
                "name", "Shoes",
                "price", 100,
                "stock", 100
        ));
    }

    private Id createCoupon() {
        return saveInstance("Coupon", Map.of(
                "title", "10 Yuan Off", "discount", 10
        ));
    }

    protected Id saveInstance(String className, Map<String, Object> fields) {
        return TestUtils.doInTransaction(() -> apiClient.saveInstance(className, fields));
    }

    protected Object callMethod(Object qualifier, String methodName, List<Object> arguments) {
        return TestUtils.doInTransaction(() -> apiClient.callMethod(qualifier, methodName, arguments));
    }

    protected ApiObject getObject(Id id) {
        return apiClient.getObject(id);
    }

    protected Object getStatic(String className, String fieldName) {
        return apiClient.getStatic(className, fieldName);
    }

}
