package tech.metavm.object.instance;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.object.type.TypeManager;
import tech.metavm.util.BootstrapUtils;
import tech.metavm.util.Constants;
import tech.metavm.util.MockUtils;
import tech.metavm.util.TestUtils;

import java.util.List;
import java.util.Map;

public class ApiServiceTest extends TestCase {

    private ApiService apiService;
    private TypeManager typeManager;
    private InstanceManager instanceManager;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        apiService = new ApiService(bootResult.entityContextFactory());
        var managers = TestUtils.createCommonManagers(bootResult);
        typeManager = managers.typeManager();
        instanceManager = managers.instanceManager();
    }

    @Override
    protected void tearDown() throws Exception {
        apiService = null;
        instanceManager = null;
        typeManager = null;
    }

    public void testNewInstance() {
        MockUtils.createShoppingTypes(typeManager);
        var title = "Shoes-40";
        var price = 100.0;
        var quantity = 100L;
        var skuId = (String) TestUtils.doInTransaction(() -> apiService.handleNewInstance(
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
        var skuId = TestUtils.doInTransaction(() -> apiService.saveInstance("SKU", Map.of(
            "name", "Shoes-40",
            "price", 100.0,
            "quantity", 100L
        )));
        // decrease quantity
        TestUtils.doInTransaction(() -> apiService.handleInstanceMethodCall(
                skuId,
                "decQuantity",
                List.of(1)
        ));
        var sku = instanceManager.get(skuId, 2).instance();
        Assert.assertEquals(99L, sku.getPrimitiveValue("quantity"));
        // create coupons
        var coupon1Id = TestUtils.doInTransaction(() -> apiService.handleNewInstance(
                "Coupon",
                List.of("5 Yuan off", 5)
        ));
        var coupon2Id = TestUtils.doInTransaction(() -> apiService.handleNewInstance(
                "Coupon",
                List.of("10 Yuan off", 10)
        ));
        // buy
        var orderId = (String) TestUtils.doInTransaction(() -> apiService.handleInstanceMethodCall(
                skuId,
                "buy",
                List.of(1, List.of(
                        Constants.prefixId(coupon1Id),
                        Constants.prefixId(coupon2Id)
                ))
        ));
        var order = instanceManager.get(orderId, 2).instance();
        Assert.assertEquals(1L, order.getPrimitiveValue("quantity"));
        Assert.assertEquals(85.0, order.getPrimitiveValue("price"));
    }

}