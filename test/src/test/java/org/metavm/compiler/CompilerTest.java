package org.metavm.compiler;

import junit.framework.TestCase;
import org.junit.Assert;
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
import java.util.Map;

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
        var source = TestUtils.getResourcePath("kiwi/Shopping.kiwi");
        var task = new CompilationTask(List.of(source), TestConstants.TARGET);
        task.parse();
        MockEnter.enterStandard(task.getProject());
        task.analyze();
        task.generate();
        deploy();

        var productId = createProduct();
        var product = getObject(productId);
        var couponsIds = createCoupons();
        var firstSkuId = (Id) product.getArray("skuList").get(0);
        var firstSku = getObject(firstSkuId);
        var orderId = (Id) callMethod(firstSkuId, "buy", List.of(1, couponsIds));
        var order = getObject(orderId);
        Assert.assertEquals(1, order.getInt("quantity"));
        Assert.assertEquals(70.0, order.getDouble("price"), 0.0001);
        for (var couponId : couponsIds) {
            var coupon = getObject(couponId);
            Assert.assertEquals("USED", coupon.getEnumConstant("state").name());
        }
        var reloadedFirstSkuDTO = getObject(firstSkuId);
        var originalQuantity = firstSku.getInt("quantity");
        var skuQuantity = reloadedFirstSkuDTO.getInt("quantity");
        Assert.assertEquals(originalQuantity - 1, skuQuantity);

    }

    private List<Id> createCoupons() {
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


    private Id createProduct() {
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
