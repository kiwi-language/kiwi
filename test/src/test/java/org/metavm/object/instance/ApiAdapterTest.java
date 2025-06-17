package org.metavm.object.instance;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.metavm.api.entity.HttpRequest;
import org.metavm.api.entity.HttpResponse;
import org.metavm.common.ErrorCode;
import org.metavm.http.HttpRequestImpl;
import org.metavm.http.HttpResponseImpl;
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
                "/api/platform-user",
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
                "/api/platform-user",
                Map.of(
                        "_id", id,
                        "loginName", "demo1"
                ),
                mockHttpRequest(),
                mockHttpResponse()
        ));
        var user = apiClient.getObject(Id.parse(id));
        assertEquals("demo1", user.get("loginName"));
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
        assertEquals(id.toString(), product.get("_id"));
        assertEquals("Product", product.get("_type"));
        assertEquals("Shoes", product.get("name"));
        //noinspection unchecked
        var skus = (List<Map<String, Object>>) product.get("SKU");
        assertEquals(1, skus.size());
        var sku = skus.getFirst();
        assertEquals("40", sku.get("variant"));
        assertEquals(100.0, (double) sku.get("price"), 0.01);
        assertEquals(100, sku.get("stock"));
    }

    public void testSaveWithChildren() {
        deploy("kiwi/children.kiwi");
        var id = (String) TestUtils.doInTransaction(() -> apiAdapter.handlePost(
                "/api/product",
                Map.of(
                        "name", "Shoes",
                        "SKU", List.of(
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
        var id = TestUtils.doInTransaction(() -> apiClient.saveInstance("PlatformUser",
                Map.of(
                        "loginName", "demo",
                        "password", "123456"
                )
        ));
        var r = TestUtils.doInTransaction(() -> apiAdapter.handlePost(
                "/api/platform-user/verify",
                Map.of(
                        "_id", id.toString(),
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
        assertEquals(id, user.get("_id"));
        assertEquals("Product", user.get("_type"));
        assertEquals("MacBook Pro", user.get("name"));
        assertNull(user.get("password"));

        var result1 = (SearchResult) apiAdapter.handlePost(
                "/api/product/_search",
                Map.of(
                        "name", "MacBook",
                        "_page", 2
                ),
                mockHttpRequest(),
                mockHttpResponse()
        );
        assertEquals(0, result1.items().size());
    }

    private String saveUser() {
        return TestUtils.doInTransaction(() -> apiClient.saveInstance("PlatformUser",
                Map.of(
                        "loginName", "demo",
                        "password", "123456"
                )
        )).toString();
    }

    private String saveProduct() {
        return TestUtils.doInTransaction(() -> apiClient.saveInstance("Product",
                Map.of(
                        "name", "MacBook Pro",
                        "price", 14000,
                        "stock", 100
                )
        )).toString();
    }

    public void testDelete() {
        deploy("kiwi/User.kiwi");
        var id = saveUser();
        TestUtils.doInTransactionWithoutResult(() -> apiAdapter.handleDelete("/api/platform-user/" + id));
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