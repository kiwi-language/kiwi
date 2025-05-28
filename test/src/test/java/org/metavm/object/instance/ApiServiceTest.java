package org.metavm.object.instance;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.common.ErrorCode;
import org.metavm.object.instance.core.ApiObject;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.TypeManager;
import org.metavm.util.*;

import java.util.List;
import java.util.Map;

import static org.metavm.util.ApiClient.KEY_DOLLAR_ID;

@Slf4j
public class ApiServiceTest extends TestCase {

    private TypeManager typeManager;
    private SchedulerAndWorker schedulerAndWorker;
    private ApiClient apiClient;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        apiClient = new ApiClient(new ApiService(bootResult.entityContextFactory(), bootResult.metaContextCache(),
                new InstanceQueryService(bootResult.instanceSearchService())));
        var managers = TestUtils.createCommonManagers(bootResult);
        typeManager = managers.typeManager();
        schedulerAndWorker = bootResult.schedulerAndWorker();
    }

    @Override
    protected void tearDown() throws Exception {
        apiClient = null;
        schedulerAndWorker = null;
        typeManager = null;
    }

    public void testNewInstance() {
        MockUtils.createShoppingTypes(typeManager, schedulerAndWorker);
        var title = "Shoes-40";
        var price = 100.0;
        var quantity = 100;
        var skuId = TestUtils.doInTransaction(() -> apiClient.newInstance(
                "SKU", List.of(title, price, quantity)
        ));
        var sku = apiClient.getObject(skuId);
        Assert.assertEquals(title, sku.getString("name"));
        Assert.assertEquals(price, sku.getDouble("price"), 0.0001);
        Assert.assertEquals(quantity, sku.getInt("quantity"));
    }
    public void testHandleInstanceMethodCall() {
        MockUtils.createShoppingTypes(typeManager, schedulerAndWorker);
//        var skuId = (String) TestUtils.doInTransaction(() -> apiService.handleNewInstance(
//                "SKU", List.of("Shoes-40", 100.0, 100L)
//        ));
        var skuId = TestUtils.doInTransaction(() -> apiClient.saveInstance("SKU", Map.of(
            "name", "Shoes-40",
            "price", 100.0,
            "quantity", 100
        )));
        // decrease quantity
        TestUtils.doInTransaction(() -> apiClient.callMethod(
                skuId,
                "decQuantity",
                List.of(1)
        ));
        var sku = apiClient.getObject(skuId);
        Assert.assertEquals(99, sku.getInt("quantity"));
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
        var orderId = (Id) TestUtils.doInTransaction(() -> apiClient.callMethod(
                skuId,
                "buy",
                List.of(1, List.of(coupon1Id, coupon2Id))
        ));
        var order = apiClient.getObject(orderId);
        Assert.assertEquals(1, order.getInt("quantity"));
        Assert.assertEquals(85.0, order.getDouble("price"), 0.0001);
    }

    public void testMethodCallWithMap() {
        MockUtils.createShoppingTypes(typeManager, schedulerAndWorker);
        var skuId = TestUtils.doInTransaction(() -> apiClient.saveInstance("SKU", Map.of(
                "name", "Shoes-40",
                "price", 100.0,
                "quantity", 100
        )));
        TestUtils.doInTransaction(() -> apiClient.callMethod(
                skuId,
                "decQuantity",
                Map.of("quantity", 1)
        ));
        var sku = apiClient.getObject(skuId);
        Assert.assertEquals(99, sku.getInt("quantity"));
    }


    public void testDelete() {
        MockUtils.assemble("kiwi/del/del.kiwi", typeManager, schedulerAndWorker);
        var id = saveInstance("del.Foo", Map.of("name", "foo"));
        TestUtils.waitForEsSync(schedulerAndWorker);
        var r = apiClient.search("del.Foo", Map.of(), 1, 20);
        assertEquals(1, r.total());
        delete(id);
        try {
            getObject(id);
            fail("Should have been removed");
        }
        catch (BusinessException e) {
            assertSame(ErrorCode.INSTANCE_NOT_FOUND, e.getErrorCode());
        }
        TestUtils.waitForEsSync(schedulerAndWorker);
        var r1 = apiClient.search("del.Foo", Map.of(), 1, 20);
        assertEquals(0, r1.total());
    }

    public void testUpdate() {
        MockUtils.assemble("kiwi/foo.kiwi", typeManager, schedulerAndWorker);
        var id = saveInstance("Foo", Map.of("name", "foo"));
        var foo = getObject(id);
        assertEquals("foo", foo.get("name"));
        saveInstance("Foo", Map.of(KEY_DOLLAR_ID, id, "name", "foo1", "Bar", List.of(
                Map.of("name", "bar")
        )));
        foo = getObject(id);
        assertEquals("foo1", foo.get("name"));
        var bars = foo.getChildren("Bar");
        assertEquals(1, bars.size());
        var bar = bars.getFirst();
        assertEquals("bar", bar.get("name"));
        var barId = bar.id();
        saveInstance("Foo", Map.of(
                KEY_DOLLAR_ID, id, "name", "foo1", "Bar", List.of(
                        Map.of(KEY_DOLLAR_ID, barId, "name", "bar1")
                )
        ));
        foo = getObject(id);
        bars = foo.getChildren("Bar");
        assertEquals(1, bars.size());
        assertEquals("bar1", bars.getFirst().get("name"));
    }

    public void testSummary() {
        MockUtils.assemble("kiwi/summary/summary.kiwi", typeManager, schedulerAndWorker);
        var productId = saveInstance("summary.Product",
                Map.of("name", "MacBook Pro", "price", 14000, "stock", 100)
        );
        var orderId = saveInstance("summary.Order",
                Map.of("product", productId, "quantity", 1)
        );
        var order = getObject(orderId);
        //noinspection unchecked
        var productRef = ((Map<String, Object>) order.getMap().get("fields")).get("product");
        assertEquals(Map.of("id", productId.toString(), "type", "summary.Product", "summary", "MacBook Pro"), productRef);
    }

    private Id saveInstance(String className, Map<String, Object> map) {
        return TestUtils.doInTransaction(() -> apiClient.saveInstance(className, map));
    }

    public void delete(Id id) {
        TestUtils.doInTransactionWithoutResult(() -> apiClient.delete(id));
    }

    public ApiObject getObject(Id id) {
        return apiClient.getObject(id);
    }

}