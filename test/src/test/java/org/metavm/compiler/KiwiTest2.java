package org.metavm.compiler;

import org.metavm.compiler.util.List;
import org.metavm.object.instance.core.Id;
import org.metavm.util.ApiNamedObject;
import org.metavm.util.BusinessException;
import org.metavm.util.TestUtils;

import java.util.Map;

public class KiwiTest2 extends KiwiTestBase {

    public void testEnumToString() {
        deploy("kiwi/to_string/enum_to_string.kiwi");
        var r = callMethod(ApiNamedObject.of("lab"), "formatMoney", List.of(
                100, ApiNamedObject.of("to_string.Currency", "CNY")
        ));
        assertEquals("100.0 CNY", r);
    }

    public void testException() {
        deploy("kiwi/exception/exception.kiwi");
        try {
            callMethod(ApiNamedObject.of("lab"), "raise", List.of("error"));
        } catch (BusinessException e) {
            assertEquals("error", e.getMessage());
        }
    }

    public void testChildObjectIndex() {
        deploy("kiwi/shopping.kiwi");
        var productId = saveInstance("Product", Map.of(
                "name", "Shoes",
                "price", 100,
                "stock", 100
        ));
        var orderService = ApiNamedObject.of("orderService");
        var orderId = (Id) callMethod(orderService, "placeOrder", Map.of(
           "product", productId,
           "quantity", 1
        ));
        TestUtils.waitForEsSync(schedulerAndWorker);
        var items = (java.util.List<?>) callMethod(orderService, "findOrderItemsByProduct", List.of(productId));
        assertEquals(1, items.size());

        callMethod(orderService, "confirmOrder", List.of(orderId));
        TestUtils.waitForEsSync(schedulerAndWorker);
        var order = getObject(orderId);
        assertEquals("CONFIRMED", ((ApiNamedObject) order.get("status")).name());
    }

    public void testParentAccess() {
        deploy("kiwi/parent/parent_access.kiwi");
        var productId = saveInstance("parent.Product", Map.of("name", "Shoes"));
        var orderService = ApiNamedObject.of("orderService");
        var orderId = (Id) callMethod(
                orderService,
                "placeOrder",
                List.of(productId)
        );
        var orders = (java.util.List<?>) callMethod(
                orderService,
                "findProductOrders",
                List.of(productId)
        );
        assertEquals(1, orders.size());
        assertEquals(orderId, orders.getFirst());
    }

    public void testChildrenAccess() {
        deploy("kiwi/children/children_access.kiwi");
        var productId = saveInstance("children.Product", Map.of(
                "name", "Shoes",
                "stock", 100
        ));
        var orderService = ApiNamedObject.of("orderService");
        var orderId = (Id) callMethod(
                orderService,
                "placeOrder",
                List.of(productId, 1)
        );
        assertEquals(99, getObject(productId).get("stock"));
        callMethod(
                orderService,
                "cancelOrder",
                List.of(orderId)
        );
        assertEquals(100, getObject(productId).get("stock"));
    }

    public void testIdAccess() {
        deploy("kiwi/id/id_access.kiwi");
        var id = saveInstance("id.Foo", Map.of());
        assertEquals(id.toString(), callMethod(id, "getId", List.of()));
    }

}
