package tech.metavm.object.instance;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.object.type.TypeManager;
import tech.metavm.util.ApiClient;
import tech.metavm.util.BootstrapUtils;
import tech.metavm.util.MockUtils;
import tech.metavm.util.TestUtils;

import java.util.List;
import java.util.Map;

public class ApiServiceTest extends TestCase {

    private TypeManager typeManager;
    private InstanceManager instanceManager;
    private ApiClient apiClient;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        apiClient = new ApiClient(new ApiService(bootResult.entityContextFactory()));
        var managers = TestUtils.createCommonManagers(bootResult);
        typeManager = managers.typeManager();
        instanceManager = managers.instanceManager();
    }

    @Override
    protected void tearDown() throws Exception {
        apiClient = null;
        instanceManager = null;
        typeManager = null;
    }

    public void testNewInstance() {
        MockUtils.createShoppingTypes(typeManager);
        var title = "Shoes-40";
        var price = 100.0;
        var quantity = 100L;
        var skuId = (String) TestUtils.doInTransaction(() -> apiClient.newInstance(
                "SKU", List.of(title, price, quantity)
        ));
        var sku = instanceManager.get(skuId, 2).instance();
        Assert.assertEquals(title, sku.getPrimitiveValue("name"));
        Assert.assertEquals(price, sku.getPrimitiveValue("price"));
        Assert.assertEquals(quantity, sku.getPrimitiveValue("quantity"));
    }

    public void testHandleInstanceMethodCall() {
        MockUtils.createShoppingTypes(typeManager);
//        var skuId = (String) TestUtils.doInTransaction(() -> apiService.handleNewInstance(
//                "SKU", List.of("Shoes-40", 100.0, 100L)
//        ));
        var skuId = TestUtils.doInTransaction(() -> apiClient.saveInstance("SKU", Map.of(
            "name", "Shoes-40",
            "price", 100.0,
            "quantity", 100L
        )));
        // decrease quantity
        TestUtils.doInTransaction(() -> apiClient.callInstanceMethod(
                skuId,
                "decQuantity",
                List.of(1)
        ));
        var sku = instanceManager.get(skuId, 2).instance();
        Assert.assertEquals(99L, sku.getPrimitiveValue("quantity"));
        // create coupons
        var coupon1Id = TestUtils.doInTransaction(() -> apiClient.newInstance(
                "Coupon",
                List.of("5 Yuan off", 5)
        ));
        var coupon2Id = TestUtils.doInTransaction(() -> apiClient.newInstance(
                "Coupon",
                List.of("10 Yuan off", 10)
        ));
        // buy
        var orderId = (String) TestUtils.doInTransaction(() -> apiClient.callInstanceMethod(
                skuId,
                "buy",
                List.of(1, List.of(coupon1Id, coupon2Id))
        ));
        var order = instanceManager.get(orderId, 2).instance();
        Assert.assertEquals(1L, order.getPrimitiveValue("quantity"));
        Assert.assertEquals(85.0, order.getPrimitiveValue("price"));
    }

}