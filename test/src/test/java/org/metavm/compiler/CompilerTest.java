package org.metavm.compiler;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.compiler.util.CompilationException;
import org.metavm.compiler.util.List;
import org.metavm.compiler.util.MockEnter;
import org.metavm.ddl.CommitState;
import org.metavm.object.instance.ApiService;
import org.metavm.object.instance.InstanceQueryService;
import org.metavm.object.instance.core.ApiObject;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.TypeManager;
import org.metavm.util.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@Slf4j
public class CompilerTest extends TestCase {

    private TypeManager typeManager;
    private SchedulerAndWorker schedulerAndWorker;
    private ApiClient apiClient;

    @Override
    protected void setUp() throws Exception {
        var result = BootstrapUtils.bootstrap();
        typeManager = TestUtils.createCommonManagers(result).typeManager();
        schedulerAndWorker = result.schedulerAndWorker();
        apiClient = new ApiClient(new ApiService(
                result.entityContextFactory(),
                result.metaContextCache(),
                new InstanceQueryService(result.instanceSearchService())
        ));
        ContextUtil.setAppId(TestConstants.APP_ID);
    }

    @Override
    protected void tearDown() throws Exception {
        typeManager = null;
        schedulerAndWorker = null;
        apiClient = null;
    }

    public void test() {
        var source = TestUtils.getResourcePath("kiwi/shopping.kiwi");
        var task = CompilationTaskBuilder.newBuilder(List.of(Path.of(source)), Path.of(TestConstants.TARGET)).build();
        task.parse();
        MockEnter.enterStandard(task.getProject());
        task.analyze();
        if (task.getErrorCount() > 0)
            throw new CompilationException("Compilation failed");
        task.generate();
        deploy();

        var productId = createProduct();
        var product = getObject(productId);
        var couponId = createCoupon();
        var orderService = ApiNamedObject.of("orderService");
        var orderId = (Id) callMethod(orderService, "placeOrder", List.of(productId, 1, couponId));
        var order = getObject(orderId);
        var orderItem = order.getChildren("Item").getFirst();
        Assert.assertEquals(1, orderItem.getInt("quantity"));
        Assert.assertEquals(90.0, order.getDouble("totalPrice"), 0.0001);
        var coupon = getObject(couponId);
        assertEquals(true, coupon.get("used"));
        var reloadedProduct = getObject(productId);
        var originalQuantity = product.getInt("stock");
        var skuQuantity = reloadedProduct.getInt("stock");
        Assert.assertEquals(originalQuantity - 1, skuQuantity);

    }

    private Id createCoupon() {
        return saveInstance("Coupon", Map.of(
                "title", "10 Yuan Off", "discount", 10
        ));
    }


    private Id createProduct() {
        return saveInstance("Product", Map.of(
                "name", "Shoes",
                "price", 100,
                "stock", 100
        ));
    }



    private void deploy() {
        var commitId = TestUtils.doInTransaction(() -> {
            try(var input = new FileInputStream(TestConstants.TARGET + "/target.mva")) {
                return typeManager.deploy(input);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        TestUtils.waitForDDLState(CommitState.COMPLETED, schedulerAndWorker);
    }

    protected Id saveInstance(String className, Map<String, Object> fields) {
        return TestUtils.doInTransaction(() -> apiClient.saveInstance(className, fields));
    }

    protected Object callMethod(Object qualifier, String methodName, java.util.List<Object> arguments) {
        return TestUtils.doInTransaction(() -> apiClient.callMethod(qualifier, methodName, arguments));
    }

    protected ApiObject getObject(Id id) {
        return apiClient.getObject(id);
    }

}
