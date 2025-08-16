package org.metavm.object.instance;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.metavm.application.Application;
import org.metavm.common.ErrorCode;
import org.metavm.entity.EntityContextFactory;
import org.metavm.object.instance.core.ApiObject;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.type.TypeManager;
import org.metavm.util.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.metavm.util.ApiClient.KEY_DOLLAR_ID;

@Slf4j
public class ApiServiceTest extends TestCase {

    private TypeManager typeManager;
    private SchedulerAndWorker schedulerAndWorker;
    private ApiClient apiClient;
    private ApiService apiService;
    private EntityContextFactory entityContextFactory;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        apiService = new ApiService(bootResult.entityContextFactory(), bootResult.metaContextCache(),
                new InstanceQueryService(bootResult.instanceSearchService()));
        apiClient = new ApiClient(apiService);
        var managers = TestUtils.createCommonManagers(bootResult);
        typeManager = managers.typeManager();
        schedulerAndWorker = bootResult.schedulerAndWorker();
        entityContextFactory = bootResult.entityContextFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        apiClient = null;
        apiService = null;
        schedulerAndWorker = null;
        typeManager = null;
        entityContextFactory = null;
    }

    public void testNewInstance() {
        deploy("kiwi/shopping.kiwi");
        var name = "Shoes";
        var price = 100.0;
        var stock = 100;
        var productId = saveInstance("Product", Map.of(
           "name", name, "price", price, "stock", stock
        ));
        var product = apiClient.getObject(productId);
        Assert.assertEquals(name, product.getString("name"));
        Assert.assertEquals(price, product.getDouble("price"), 0.0001);
        Assert.assertEquals(stock, product.getInt("stock"));
    }

    public void testHandleInstanceMethodCall() {
        deploy("kiwi/shopping.kiwi");
        var productId = saveInstance("Product", Map.of(
            "name", "Shoes",
            "price", 100.0,
            "stock", 100
        ));
        callMethod(
                productId,
                "reduceStock",
                List.of(1)
        );
        var product = apiClient.getObject(productId);
        Assert.assertEquals(99, product.getInt("stock"));
        var couponId = saveInstance(
                "Coupon",
                Map.of("title", "10 Yuan off", "discount", 10)
        );
        var orderId = (Id) callMethod(
                ApiNamedObject.of("orderService"),
                "placeOrder",
                List.of(productId, 1, couponId)
        );
        var order = apiClient.getObject(orderId);
        Assert.assertEquals(1, order.getChildren("Item").getFirst().getInt("quantity"));
        Assert.assertEquals(90.0, order.getDouble("totalPrice"), 0.0001);
    }

    public void testMethodCallWithMap() {
        deploy("kiwi/shopping.kiwi");
        var productId = saveInstance("Product", Map.of(
                "name", "Shoes",
                "price", 100.0,
                "stock", 100
        ));
        TestUtils.doInTransaction(() -> apiClient.callMethod(
                productId,
                "reduceStock",
                Map.of("quantity", 1)
        ));
        var product = apiClient.getObject(productId);
        Assert.assertEquals(99, product.getInt("stock"));
    }


    public void testDelete() {
        MockUtils.assemble("kiwi/del/del.kiwi", typeManager, schedulerAndWorker);
        var id = saveInstance("del.Foo", Map.of("name", "foo"));
        TestUtils.waitForEsSync(schedulerAndWorker);
        var r = apiClient.search("del.Foo", Map.of(), 1, 20);
        assertEquals(1, r.total());
        deleteObject(id);
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

    public void testSearchWithCreatedId() {
        MockUtils.assemble("kiwi/search/search.kiwi", typeManager, schedulerAndWorker);
        var id = saveInstance("search.SearchFoo", Map.of("name", "Foo"));
        var r = apiClient.search("search.SearchFoo", Map.of(), 1, 20, id);
        assertEquals(1, r.total());
        assertEquals(1, r.items().size());
        assertEquals(id, r.items().getFirst().id());

        TestUtils.waitForEsSync(schedulerAndWorker);

        // Ensure search condition is applied properly
        var r1 = apiClient.search("search.SearchFoo", Map.of("name", "Bar"), 1, 20, id);
        assertEquals(0, r1.total());
        assertEquals(0, r1.items().size());

        // Ensure providing newly created ID after document sync doesn't cause problems
        var r2 = apiClient.search("search.SearchFoo", Map.of(), 1, 20, id);
        assertEquals(1, r2.total());
        assertEquals(1, r2.items().size());
        assertEquals(id, r2.items().getFirst().id());
    }

    public void testAppStatusCheck() {
        MockUtils.assemble("kiwi/foo.kiwi", typeManager, schedulerAndWorker);
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var platformCtx = entityContextFactory.newContext(Constants.PLATFORM_APP_ID)) {
                var app = platformCtx.getEntity(Application.class, PhysicalId.of(TestConstants.APP_ID, 0));
                app.deactivate();
                platformCtx.finish();
            }
        });
        try {
            saveInstance("Foo", Map.of("name", "foo"));
            fail("Should have failed because application is inactive");
        }
        catch (BusinessException e) {
            assertSame(ErrorCode.APP_NOT_ACTIVE, e.getErrorCode());
        }
    }

    public void testSearchAfterRemoval() {
        MockUtils.assemble("kiwi/search/search.kiwi", typeManager, schedulerAndWorker);
        var id = saveInstance("search.SearchFoo", Map.of("name", "Foo"));
        TestUtils.waitForEsSync(schedulerAndWorker);
        deleteObject(id);
        var r = apiClient.search("search.SearchFoo", Map.of(), 1, 20, null);
        assertEquals(0, r.total());
    }

    public void testMultiGet() {
        MockUtils.assemble("kiwi/foo.kiwi", typeManager, schedulerAndWorker);
        var ids = new ArrayList<Id>();
        for (int i = 0; i < 3; i++) {
            ids.add(saveInstance("Foo", Map.of("name", "foo" + i)));
        }
        var objects = apiClient.multiGet(ids);
        assertEquals(ids.size(), objects.size());
        for (int i = 0; i < ids.size(); i++) {
            var o = objects.get(i);
            assertEquals(ids.get(i), o.id());
            assertEquals("foo" + i, o.get("name"));
        }

        var objects1 = apiService.multiGet(Utils.map(ids, Id::toString),
                true, true);
        assertNotNull(objects1.getFirst().get("summary"));
        assertNull(objects1.getFirst().get("children"));
        assertNull(objects1.getFirst().get("field"));
    }

    public void testReturnFullObject() {
        deploy("kiwi/shopping.kiwi");
        var productId = saveInstance("Product", Map.of(
                "name", "Shoes", "price", 100, "stock", 100
        ));
        var order = TestUtils.doInTransaction(() -> apiClient.callMethod(
                ApiNamedObject.of("orderService"),
                "placeOrder",
                Arrays.asList(
                        productId,
                        1,
                        null
                ),
                true,
                Map.of()
        ));
        MatcherAssert.assertThat(order, CoreMatchers.instanceOf(ApiObject.class));
    }

    private void deploy(String source) {
        MockUtils.assemble(source, typeManager, schedulerAndWorker);
    }

    private Id saveInstance(String className, Map<String, Object> map) {
        return TestUtils.doInTransaction(() -> apiClient.saveInstance(className, map));
    }

    private void deleteObject(Id id) {
        TestUtils.doInTransactionWithoutResult(() -> apiClient.delete(id));
    }

    private ApiObject getObject(Id id) {
        return apiClient.getObject(id);
    }

    private Object callMethod(Object qualifier, String methodName, List<Object> args) {
        return TestUtils.doInTransaction(() -> apiClient.callMethod(
                qualifier,
                methodName,
                args
        ));
    }

}