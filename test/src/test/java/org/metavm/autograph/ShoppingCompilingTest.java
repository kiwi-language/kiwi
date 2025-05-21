package org.metavm.autograph;

import org.junit.Assert;
import org.metavm.object.instance.core.Id;
import org.metavm.util.ApiNamedObject;

import java.util.List;
import java.util.Map;

public class ShoppingCompilingTest extends CompilerTestBase {

    public static final String SHOPPING_SOURCE_ROOT = "shopping";

    public void testShopping() {
        compileTwice(SHOPPING_SOURCE_ROOT);
        submit(() -> {
            var productId = saveInstance(
                    "org.metavm.lab.shopping.AstProduct",
                    Map.of(
                            "title", "shoes",
                            "price", 100,
                            "inventory", 100
                    )
            );
            var couponId = saveInstance(
                    "org.metavm.lab.shopping.AstDirectCoupon",
                    Map.of(
                            "discount", 5,
                            "state",
                            ApiNamedObject.of("org.metavm.lab.shopping.AstCouponState", "UNUSED"),
                            "product", productId
                    )
            );
            var orderId = (Id) callMethod(
                    productId,
                    "buy",
                    List.of(1, List.of(couponId))
            );
            var order = getObject(orderId);
            var price = order.getLong("price");
            var orderCoupons = order.getArray("coupons");
            Assert.assertEquals(1, orderCoupons.size());
            Assert.assertEquals(95L, price);
        });
    }

}
