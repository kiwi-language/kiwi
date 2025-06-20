package org.metavm.object.instance;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.metavm.api.entity.HttpRequest;
import org.metavm.api.entity.HttpResponse;
import org.metavm.common.ErrorCode;
import org.metavm.http.HttpRequestImpl;
import org.metavm.http.HttpResponseImpl;
import org.metavm.object.instance.core.ApiObject;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.rest.SearchResult;
import org.metavm.object.type.TypeManager;
import org.metavm.util.*;

import java.util.List;
import java.util.Map;

@Slf4j
public class ApiAdapterTest extends TestCase {

    private ApiAdapter apiAdapter;
    private ApiClient apiClient;
    private SchedulerAndWorker schedulerAndWorker;
    private TypeManager typeManager;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        var apiService = new ApiService(
                bootResult.entityContextFactory(),
                bootResult.metaContextCache(),
                new InstanceQueryService(bootResult.instanceSearchService())
        );
        apiAdapter = new ApiAdapter(bootResult.entityContextFactory(), apiService);
        apiClient = new ApiClient(apiService);
        schedulerAndWorker = bootResult.schedulerAndWorker();
        typeManager = TestUtils.createCommonManagers(bootResult).typeManager();
    }

    @Override
    protected void tearDown() throws Exception {
        apiAdapter = null;
        apiClient = null;
        schedulerAndWorker = null;
        typeManager = null;
    }

    public void testCreate() {
        deploy("kiwi/User.kiwi");
        var id = (String) TestUtils.doInTransaction(() -> apiAdapter.handlePost(
                "/api/user",
                Map.of(
                        "loginName", "demo",
                        "password", "123456"
                ),
                mockHttpRequest(),
                mockHttpResponse()
        ));
        var user = apiClient.getObject(Id.parse(id));
        assertEquals("demo", user.get("loginName"));
    }

    public void testUpdate() {
        deploy("kiwi/User.kiwi");
        var id = saveUser();
        TestUtils.doInTransaction(() -> apiAdapter.handlePost(
                "/api/user",
                Map.of(
                        "id", id,
                        "loginName", "demo1"
                ),
                mockHttpRequest(),
                mockHttpResponse()
        ));
        var user = apiClient.getObject(Id.parse(id));
        assertEquals("demo1", user.get("loginName"));
    }

    public void testRefSummary() {
        deploy("kiwi/User.kiwi");
        var uId = saveInstance("User", Map.of("loginName", "demo", "password", "123456"));
        var appId = saveInstance("Application", Map.of("name", "demo", "owner", Id.parse(uId)));
        var app = apiAdapter.handleGet("/api/application/" + appId);
        assertEquals("demo", app.get("ownerLoginName"));

        var r = (SearchResult) apiAdapter.handlePost(
                "/api/application/_search",
                Map.of(
                        "newlyChangedId", appId
                ),
                mockHttpRequest(),
                mockHttpResponse()
        );
        assertEquals(1, r.items().size());
        //noinspection rawtypes
        assertEquals("demo", ((Map) r.items().getFirst()).get("ownerLoginName"));
    }

    public void testGet() {
        deploy("kiwi/children.kiwi");
        var id = TestUtils.doInTransaction(() -> apiClient.saveInstance("Product", Map.of(
           "name", "Shoes",
           "SKU", List.of(
                   Map.of(
                           "variant", "40",
                           "price", 100,
                           "stock", 100
                   )
                )
        )));
        var product = apiAdapter.handleGet("/api/product/" + id);
        assertEquals(id.toString(), product.get("id"));
        assertEquals("Shoes", product.get("name"));
        //noinspection unchecked
        var skus = (List<Map<String, Object>>) product.get("skus");
        assertEquals(1, skus.size());
        var sku = skus.getFirst();
        assertEquals("40", sku.get("variant"));
        assertEquals(100.0, (double) sku.get("price"), 0.01);
        assertEquals(100, sku.get("stock"));
    }

    public void testEmptyIdString() {
        deploy("kiwi/shopping2.kiwi");
        var productId = saveInstance("Product", Map.of(
                "name", "Shoes", "price", 100, "stock", 100)
        );
        var orderId = TestUtils.doInTransaction(() -> apiAdapter.handlePost(
                "/api/order-service/place-order",
                Map.of(
                        "productId", productId,
                        "quantity", 1,
                        "couponId", ""
                ),
                mockHttpRequest(),
                mockHttpResponse()
        ));
        var order = getObject(orderId.toString());
        assertEquals(100.0, order.getDouble("totalPrice"), 0.01);
    }

    public void testAutomaticTypeConversion() {
        deploy("kiwi/shopping2.kiwi");
        var id = (String) TestUtils.doInTransaction(() -> apiAdapter.handlePost(
                "/api/product",
                Map.of(
                        "name", "Shoes",
                        "price", "100",
                        "stock", "100"
                ),
                mockHttpRequest(),
                mockHttpResponse()
        ));
        var product = getObject(id);
        assertEquals(100.0, product.getDouble("price"), 0.01);
        assertEquals(100, product.get("stock"));
    }

    public void testSaveWithChildren() {
        deploy("kiwi/children.kiwi");
        var id = (String) TestUtils.doInTransaction(() -> apiAdapter.handlePost(
                "/api/product",
                Map.of(
                        "name", "Shoes",
                        "skus", List.of(
                                Map.of(
                                        "variant", "40",
                                        "price", 100,
                                        "stock", 100
                                )
                        )
                ),
                mockHttpRequest(),
                mockHttpResponse()
        ));
        var product = apiClient.getObject(Id.parse(id));
        assertEquals("Shoes", product.get("name"));
        var skus = product.getChildren("SKU");
        assertEquals(1, skus.size());
        var sku = skus.getFirst();
        assertEquals("40", sku.get("variant"));
        assertEquals(100.0, sku.getDouble("price"), 0.01);
        assertEquals(100, sku.getInt("stock"));
    }

    public void testInvoke() {
        deploy("kiwi/User.kiwi");
        var id = TestUtils.doInTransaction(() -> apiClient.saveInstance("User",
                Map.of(
                        "loginName", "demo",
                        "password", "123456"
                )
        ));
        var r = TestUtils.doInTransaction(() -> apiAdapter.handlePost(
                "/api/user/verify",
                Map.of(
                        "userId", id.toString(),
                        "password", "123456"
                ),
                mockHttpRequest(),
                mockHttpResponse()
        ));
        assertEquals(true, r);
    }

    public void testSearch() {
        deploy("kiwi/simple_shopping.kiwi");
        var id = saveProduct();
        TestUtils.waitForEsSync(schedulerAndWorker);
        var result = (SearchResult) apiAdapter.handlePost(
                "/api/product/_search",
                Map.of(
                        "name", "MacBook",
                        "price", List.of(10000, 150000)
                ),
                mockHttpRequest(),
                mockHttpResponse()
        );
        assertEquals(1, result.total());
        //noinspection unchecked
        var user = (Map<String, Object>) result.items().getFirst();
        assertEquals(id, user.get("id"));
        assertEquals("MacBook Pro", user.get("name"));
        assertNull(user.get("password"));

        var result1 = (SearchResult) apiAdapter.handlePost(
                "/api/product/_search",
                Map.of(
                        "name", "MacBook",
                        "page", 2
                ),
                mockHttpRequest(),
                mockHttpResponse()
        );
        assertEquals(0, result1.items().size());

        var id2 = saveInstance("Product",
                Map.of(
                        "name", "MacBook Air",
                        "price", 8500,
                        "stock", 100
                )
        );

        var result2 = (SearchResult) apiAdapter.handlePost(
                "/api/product/_search",
                Map.of(
                        "name", "MacBook",
                        "page", 1,
                        "newlyChangedId", id2
                ),
                mockHttpRequest(),
                mockHttpResponse()
        );
        assertEquals(2, result2.items().size());
    }

    public void testSearchWithRef() {
        deploy("kiwi/User.kiwi");
        var userId = saveUser();
        var appId = saveInstance("Application", Map.of(
                "name", "demo",
                "owner", Id.parse(userId)
        ));
        TestUtils.waitForEsSync(schedulerAndWorker);
        var r = (SearchResult) apiAdapter.handlePost(
                "/api/application/_search",
                Map.of(
                        "ownerId", userId
                ),
                mockHttpRequest(),
                mockHttpResponse()
        );
        assertEquals(1, r.total());
        //noinspection rawtypes
        assertEquals(appId, ((Map) r.items().getFirst()).get("id"));
    }

    public void testSearchIncludeChildren() {
        deploy("kiwi/children.kiwi");
        var id = saveInstance("Product", Map.of(
                "name", "Shoes",
                "SKU", List.of(
                        Map.of(
                                "variant", "40",
                                "price", 100,
                                "stock", 100
                        )
                )
        ));
        var r = (SearchResult) apiAdapter.handlePost(
                "/api/product/_search",
                Map.of(
                        "includeChildren", true,
                        "newlyChangedId", id
                ),
                mockHttpRequest(),
                mockHttpResponse()
        );
        assertEquals(1, r.total());
        //noinspection unchecked
        var item = (Map<String, Object>) r.items().getFirst();
        var skus = item.get("skus");
        assertNotNull(skus);
        assertEquals(1, ((List<?>) skus).size());
    }

    private String saveUser() {
        return TestUtils.doInTransaction(() -> apiClient.saveInstance("User",
                Map.of(
                        "loginName", "demo",
                        "password", "123456"
                )
        )).toString();
    }

    private ApiObject getObject(String id) {
        return apiClient.getObject(Id.parse(id));
    }

    private String saveInstance(String className, Map<String, Object> object) {
        return TestUtils.doInTransaction(() -> apiClient.saveInstance(className, object)).toString();
    }

    private String saveProduct() {
        return saveInstance("Product",
                Map.of(
                        "name", "MacBook Pro",
                        "price", 14000,
                        "stock", 100
                )
        );
    }

    public void testDelete() {
        deploy("kiwi/User.kiwi");
        var id = saveUser();
        TestUtils.doInTransactionWithoutResult(() -> apiAdapter.handleDelete("/api/user/" + id));
        try {
            apiClient.getObject(Id.parse(id));
            fail("Object should have been removed");
        }
        catch (BusinessException e) {
            assertSame(ErrorCode.INSTANCE_NOT_FOUND, e.getErrorCode());
        }
    }

    public void testInvokeBeanMethod() {
        deploy("kiwi/beans/foo_service.kiwi");
        var msg = TestUtils.doInTransaction(() -> apiAdapter.handlePost(
                "/api/beans/foo-service/greet",
                Map.of(),
                mockHttpRequest(),
                mockHttpResponse()
        ));
        assertEquals("Hello", msg);
    }

    public void testRefParamType() {
        deploy("kiwi/adapter/ref_param_type.kiwi");
        var id = saveInstance("adapter.Product", Map.of("name", "Shoes"));
        TestUtils.doInTransaction(() -> apiAdapter.handlePost(
                "/api/adapter/product-service/activate",
                Map.of(
                        "productId", id
                ),
                mockHttpRequest(),
                mockHttpResponse()
        ));
        var product = getObject(id);
        assertEquals("ACTIVE", ((ApiNamedObject) product.get("status")).name());

        var found = TestUtils.doInTransaction(() -> apiAdapter.handlePost(
                "/api/adapter/product-service/getFirstByStatus",
                Map.of(
                        "status", "ACTIVE"
                ),
                mockHttpRequest(),
                mockHttpResponse()
        ));
        assertEquals(id, found);
    }

    public void testRefInitArg() {
        deploy("kiwi/User.kiwi");
        var userId = saveUser();
        var appId = (String) TestUtils.doInTransaction(() -> apiAdapter.handlePost(
                "/api/application",
                Map.of(
                        "name", "demo",
                        "ownerId", userId
                ),
                mockHttpRequest(),
                mockHttpResponse()
        ));
        var app = getObject(appId);
        assertEquals(Id.parse(userId), app.get("owner"));
    }

    public void testReferenceArray() {
        deploy(("kiwi/User.kiwi"));
        var userId = saveUser();
        var appId = saveInstance("Application", Map.of(
                "name", "demo",
                "owner", Id.parse(userId)
        ));
        var app = apiAdapter.handleGet("/api/application/" + appId);
        assertEquals(List.of(userId), app.get("membersIds"));
    }

    private void deploy(String source) {
        MockUtils.assemble(source, typeManager, schedulerAndWorker);
    }
    
    private HttpRequest mockHttpRequest() {
        return new HttpRequestImpl("POST", "/api/dummy", List.of(), List.of());
    }

    private HttpResponse mockHttpResponse() {
        return new HttpResponseImpl();
    }
    
}