package org.metavm.object.instance;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.object.instance.core.ClassInstanceWrap;
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
        TestUtils.ensureStringKlassInitialized();
        MockStandardTypesInitializer.init();
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
        MockUtils.createShoppingTypes(typeManager,schedulerAndWorker);
        TestUtils.doInTransaction(() -> apiClient.saveInstance("Product", Map.of(
           "name", "shoes",
           "skuList", List.of(
                   Map.of(
                           "name", "40",
                           "price", 100,
                           "quantity", 100
                   )
                )
        )));
    }

    public void testDecAmount() {
        MockUtils.createShoppingTypes(typeManager, schedulerAndWorker);
        var productId = createProduct();
        var product = getObject(productId);
        var firstSkuId = (String) product.getArray("skuList").get(0);
        var firstSku = getObject(firstSkuId);
        int originalAmount = firstSku.getInt("quantity");
        callMethod(firstSkuId, "decQuantity", List.of(1));
        var updatedFirstSku = getObject(firstSkuId);
        int newAmount = updatedFirstSku.getInt("quantity");
        assertEquals(originalAmount - 1, newAmount);
        try {
            callMethod(firstSkuId, "decQuantity", List.of(originalAmount));
            fail("Should fail when amount is not enough");
        } catch (BusinessException e) {
            Assert.assertEquals("Out of inventory", e.getMessage());
        }
    }

    public void testBuy() {
        var shoppingTypeIds = MockUtils.createShoppingTypes(typeManager, schedulerAndWorker);
        var productId = createProduct();
        var product = getObject(productId);
        var couponsIds = createCoupons();
        var firstSkuId = (String) product.getArray("skuList").get(0);
        var firstSku = getObject(firstSkuId);
        var orderId = (String) callMethod(firstSkuId, "buy", List.of(1, couponsIds));
        var order = getObject(orderId);
        Assert.assertEquals(1, order.getInt("quantity"));
        Assert.assertEquals(70.0, order.getDouble("price"), 0.0001);
        for (var couponId : couponsIds) {
            var coupon = getObject(couponId);
            Assert.assertEquals("USED", coupon.getString("state"));
        }
        var reloadedFirstSkuDTO = getObject(firstSkuId);
        var originalQuantity = firstSku.getInt("quantity");
        var skuQuantity = reloadedFirstSkuDTO.getInt("quantity");
        Assert.assertEquals(originalQuantity - 1, skuQuantity);
    }

    private String createProduct() {
        return saveInstance("Product", Map.of(
                "name", "Shoes",
                "skuList", List.of(
                        Map.of(
                                "name", "40",
                                "price", 100,
                                "quantity", 100
                        ),
                        Map.of(
                                "name", "41",
                                "price", 100,
                                "quantity", 100
                        ),
                        Map.of(
                                "name", "42",
                                "price", 100,
                                "quantity", 100
                        )
                )
        ));
    }

    private List<String> createCoupons() {
        return List.of(
                saveInstance("Coupon", Map.of(
                        "name", "5 Yuan Off", "discount", 5
                )),
                saveInstance("Coupon", Map.of(
                        "name", "10 Yuan Off", "discount", 10
                )),
                saveInstance("Coupon", Map.of(
                        "name", "15 Yuan Off", "discount", 15
                ))
        );
    }

    protected String saveInstance(String className, Map<String, Object> fields) {
        return TestUtils.doInTransaction(() -> apiClient.saveInstance(className, fields));
    }

    protected Object callMethod(String qualifier, String methodName, List<Object> arguments) {
        return TestUtils.doInTransaction(() -> apiClient.callMethod(qualifier, methodName, arguments));
    }

    protected ClassInstanceWrap getObject(String id) {
        return apiClient.getObject(id);
    }

    protected Object getStatic(String className, String fieldName) {
        return apiClient.getStatic(className, fieldName);
    }

}
