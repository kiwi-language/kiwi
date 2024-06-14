package org.metavm.autograph;

import org.junit.Assert;
import org.metavm.util.TestUtils;

import java.util.List;
import java.util.Map;

import static org.metavm.util.TestUtils.doInTransaction;

public class ShoppingCompilingTest extends CompilerTestBase {

    public static final String SHOPPING_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/shopping";

    public void testShopping() {
        compileTwice(SHOPPING_SOURCE_ROOT);
        submit(() -> {
            var productType = getClassTypeByCode("org.metavm.lab.shopping.AstProduct");
            var productId = TestUtils.doInTransaction(() -> apiClient.saveInstance(
                    productType.getCodeRequired(),
                    Map.of(
                            "title", "shoes",
                            "price", 100,
                            "inventory", 100
                    )
            ));
            var directCouponType = getClassTypeByCode("org.metavm.lab.shopping.AstDirectCoupon");
            var couponStateType = getClassTypeByCode("org.metavm.lab.shopping.AstCouponState");
            var couponNormalState = TestUtils.getEnumConstantByName(couponStateType, "UNUSED");
            var couponId = TestUtils.doInTransaction(() -> apiClient.saveInstance(
                    directCouponType.getCodeRequired(),
                    Map.of(
                            "discount", 5,
                            "state", couponNormalState.getIdRequired(),
                            "product", productId
                    )
            ));
            var orderId = (String) doInTransaction(() -> apiClient.callInstanceMethod(
                    productId,
                    "buy",
                    List.of(1, List.of(couponId))
            ));
            var order = instanceManager.get(orderId, 1).instance();
            var price = order.getPrimitiveValue("price");
            var orderCoupons = order.getInstance("coupons");
            Assert.assertEquals(1, orderCoupons.getListSize());
            Assert.assertEquals(95L, price);
        });
    }

}
