package org.metavm.util;

import org.metavm.object.instance.core.Id;

public record ShoppingTypeIds(
        String productTypeId,
        String skuTypeId,
        String couponStateTypeId,
        String couponTypeId,
        String orderTypeId,
        String skuListType,
        String couponListType,
        String productTitleFieldId,
        String productSkuListFieldId,
        String skuTitleFieldId,
        String skuPriceFieldId,
        String skuAmountFieldId,
        String skuDecAmountMethodId,
        String skuBuyMethodId,
        String couponTitleFieldId,
        String couponDiscountFieldId,
        String couponStateFieldId,
        String orderCodeFieldId,
        String orderSkuFieldId,
        String orderAmountFieldId,
        String orderPriceFieldId,
        String orderTimeFieldId,
        String orderCouponsFieldId,
        Id couponNormalStateId,
        Id couponUsedStateId
) {
}
