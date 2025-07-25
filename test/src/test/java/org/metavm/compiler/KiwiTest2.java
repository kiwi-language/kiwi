package org.metavm.compiler;

import com.google.protobuf.Api;
import org.metavm.common.ErrorCode;
import org.metavm.object.instance.core.Id;
import org.metavm.util.ApiNamedObject;
import org.metavm.util.BusinessException;
import org.metavm.util.TestUtils;

import java.util.List;
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
        var items = (List<?>) callMethod(orderService, "findOrderItemsByProduct", List.of(productId));
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
        var orders = (List<?>) callMethod(
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

    public void testOverride() {
        deploy("kiwi/override/override.kiwi");
        var id = saveInstance("override.Sub", Map.of());
        var greeting = callMethod(id, "greet", List.of());
        assertEquals("Hi", greeting);
    }

    public void testShadowedParentMethod() {
        deploy("kiwi/children/shadowed_parent_method.kiwi");
        var id = saveInstance("children.Parent", Map.of(
                "Child", List.of(Map.of())
        ));
        var parent = getObject(id);
        var child = parent.getChildren("Child").getFirst();
        var greeting = callMethod(child.id(), "greet", List.of());
        assertEquals("Hi", greeting);
    }

    public void testIndexQuery() {
        deploy("kiwi/index/query.kiwi");
        var id = saveInstance("index.Foo", Map.of(
                "value", 1
        ));
        var found = (List<?>) callMethod(
                ApiNamedObject.of("fooService"),
                "queryFoosByValue",
                List.of(0, 2)
        );
        assertEquals(1, found.size());
        assertEquals(id, found.getFirst());
    }

    public void testGetLast() {
        deploy("kiwi/index/get_last.kiwi");
        saveInstance("index.Foo", Map.of("value", 1));
        var id = saveInstance("index.Foo", Map.of("value", 1));
        var found = callMethod(
                ApiNamedObject.of("fooService"),
                "getLastFooByValue",
                List.of(1)
        );
        assertEquals(id, found);
    }

    public void testDoubleToIntCast() {
        deploy("kiwi/cast/primitive_cast.kiwi");

        assertEquals(
                1,
                callMethod(
                        ApiNamedObject.of("lab"),
                        "longToInt",
                        List.of(1)
                ));

        assertEquals(
                1.0,
                callMethod(
                ApiNamedObject.of("lab"),
                "intToDouble",
                List.of(1)
        ));

        assertEquals(
                1,
                callMethod(
                        ApiNamedObject.of("lab"),
                        "doubleToInt",
                        List.of(1.0)
         ));

        assertEquals(
                1L,
                callMethod(
                        ApiNamedObject.of("lab"),
                        "floatToLong",
                        List.of(1.0)
        ));

        assertEquals(
                1,
                callMethod(
                        ApiNamedObject.of("lab"),
                        "intToInt",
                        List.of(1)
                ));
    }

    public void testCondExprSameType() {
        deploy("kiwi/condexpr/condexpr_same_type.kiwi");
        assertEquals(
                true,
                callMethod(
                        ApiNamedObject.of("lab"),
                        "maxGt",
                        List.of(1, 2)
                )
        );
    }

    public void testIntLongCompare() {
        deploy("kiwi/widening/int_long_cmp.kiwi");
        assertEquals(
                true,
                callMethod(
                        ApiNamedObject.of("lab"),
                        "le",
                        List.of(1, 2)
                )
        );
    }

    public void testSearchPageSizeLimit() {
        deploy("kiwi/search/search.kiwi");
        saveInstance("search.SearchFoo", Map.of(
                "name", "Foo"
        ));
        TestUtils.waitForEsSync(schedulerAndWorker);
        var r = apiClient.search("search.SearchFoo", Map.of(), 1, 10000);
        assertEquals(1, r.total());
    }

    public void testIndexKeyComputeError() {
        deploy("kiwi/index/index_key_compute_error.kiwi");
        try {
            saveInstance("index.Task", Map.of());
            fail("Should have failed");
        } catch (BusinessException e) {
            assertSame(ErrorCode.INDEX_KEY_COMPUTE_ERROR, e.getErrorCode());
        }
    }

    public void testSort() {
        deploy("kiwi/arrays/sort.kiwi");
        var r = callMethod(
                ApiNamedObject.of("lab"),
                "sort",
                List.of(List.of(
                        3, 2, 1
                ))
        );
        assertEquals(List.of(1, 2, 3), r);
    }

}
