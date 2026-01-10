package org.manul.compiler;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.manul.common.ErrorCode;
import org.manul.entity.AttributeNames;
import org.manul.entity.NumberFormats;
import org.manul.object.instance.core.Id;
import org.manul.util.ApiNamedObject;
import org.manul.util.BusinessException;
import org.manul.util.TestUtils;

import java.util.List;
import java.util.Map;

public class ManulTest2 extends ManulTestBase {

    public void testEnumToString() {
        deploy("manul/to_string/enum_to_string.mnl");
        var r = callMethod(ApiNamedObject.of("lab"), "formatMoney", List.of(
                100, ApiNamedObject.of("to_string.Currency", "CNY")
        ));
        assertEquals("100.0 CNY", r);
    }

    public void testException() {
        deploy("manul/exception/exception.mnl");
        try {
            callMethod(ApiNamedObject.of("lab"), "raise", List.of("error"));
        } catch (BusinessException e) {
            assertEquals("error", e.getMessage());
        }
    }

    public void testChildObjectIndex() {
        deploy("manul/shopping.mnl");
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
        deploy("manul/parent/parent_access.mnl");
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
        deploy("manul/children/children_access.mnl");
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
        deploy("manul/id/id_access.mnl");
        var id = saveInstance("id.Foo", Map.of());
        assertEquals(id.toString(), callMethod(id, "getId", List.of()));
    }

    public void testOverride() {
        deploy("manul/override/override.mnl");
        var id = saveInstance("override.Sub", Map.of());
        var greeting = callMethod(id, "greet", List.of());
        assertEquals("Hi", greeting);
    }

    public void testShadowedParentMethod() {
        deploy("manul/children/shadowed_parent_method.mnl");
        var id = saveInstance("children.Parent", Map.of(
                "Child", List.of(Map.of())
        ));
        var parent = getObject(id);
        var child = parent.getChildren("Child").getFirst();
        var greeting = callMethod(child.id(), "greet", List.of());
        assertEquals("Hi", greeting);
    }

    public void testIndexQuery() {
        deploy("manul/index/query.mnl");
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
        deploy("manul/index/get_last.mnl");
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
        deploy("manul/cast/primitive_cast.mnl");

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
        deploy("manul/condexpr/condexpr_same_type.mnl");
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
        deploy("manul/widening/int_long_cmp.mnl");
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
        deploy("manul/search/search.mnl");
        saveInstance("search.SearchFoo", Map.of(
                "name", "Foo"
        ));
        TestUtils.waitForEsSync(schedulerAndWorker);
        var r = apiClient.search("search.SearchFoo", Map.of(), 1, 10000);
        assertEquals(1, r.total());
    }

    public void testIndexKeyComputeError() {
        deploy("manul/index/index_key_compute_error.mnl");
        try {
            saveInstance("index.Task", Map.of());
            fail("Should have failed");
        } catch (BusinessException e) {
            assertSame(ErrorCode.INDEX_KEY_COMPUTE_ERROR, e.getErrorCode());
        }
    }

    public void testSort() {
        deploy("manul/arrays/sort.mnl");
        var r = callMethod(
                ApiNamedObject.of("lab"),
                "sort",
                List.of(List.of(
                        3, 2, 1
                ))
        );
        assertEquals(List.of(1, 2, 3), r);
    }

    public void testReverse(){
        deploy("manul/arrays/reverse.mnl");
        var r = callMethod(
                ApiNamedObject.of("lab"),
                "reverse",
                List.of(List.of(
                        1, 2, 3
                ))
        );
        assertEquals(List.of(3, 2, 1), r);
    }

    public void testNegativeIndexKey() {
        deploy("manul/index/negative.mnl");
        saveInstance("index.Player", Map.of(
                "score", 100
        ));
        var leaders = (List<?>) callMethod(
                ApiNamedObject.of("playerService"),
                "getLeaderBoard",
                List.of()
        );
        assertEquals(1, leaders.size());
    }

    public void testUUID() {
        deploy("manul/uuid/uuid.mnl");
        var uuid = callMethod(
                ApiNamedObject.of("lab"),
                "generateUUID",
                List.of()
        );
        MatcherAssert.assertThat(uuid, CoreMatchers.instanceOf(String.class));
    }

    public void testNullableIndexKey() {
        deploy("manul/index/nullable_key.mnl");
        var id = saveInstance("index.Foo", Map.of());
        saveInstance("index.Foo", Map.of());
        var foo = getObject(id);
        assertNull(foo.get("name"));
        var id2 = callMethod(
                ApiNamedObject.of("fooService"),
                "findFooByName",
                Map.of()
        );
        assertEquals(id, id2);
    }

    public void testDateAnnotation() {
        deploy("manul/annotation/date.mnl");
        try (var context = newContext()) {
            context.loadKlasses();
            var cls = context.getKlassByQualifiedName("annotation.Product");
            var f = cls.getFieldByName("createdAt");
            assertEquals(NumberFormats.DATE, f.getAttribute(AttributeNames.NUMBER_FORMAT));
        }
    }

    public void testNewArrayWithElements() {
        deploy("manul/arrays/new_array_with_elems.mnl");
        var r = callMethod(
                ApiNamedObject.of("lab"),
                "newArray",
                List.of(1, 2)
        );
        assertEquals(
                List.of(1, 2),
                r
        );
    }

    public void testInternalAccess() {
        deploy("manul/access/internal.mnl");
    }

    public void testDeleteChildrenWithDeps() {
        deploy("manul/deletes/delete_children_with_deps_0.mnl");
        var id = saveInstance("deletes.App", Map.of("name", "test"));
        assertEquals(2, getObject(id).getChildren("Module").size());
        deploy("manul/deletes/delete_children_with_deps_1.mnl");
        assertEquals(0, getObject(id).getChildren("Module").size());
    }

    public void testForwardReference() {
        deploy("manul/deploy/forward_reference.mnl");
        var userId = saveInstance("deploy.User", Map.of("name", "Leen", "products", List.of()));
        var product = saveInstance("deploy.Product", Map.of(
           "name", "Shoes", "owner", userId
        ));
        assertEquals("Leen", callMethod(product, "getOwnerName", List.of()));
        assertEquals("Shoes", callMethod(userId, "getFirstProductName", List.of()));
    }

}
