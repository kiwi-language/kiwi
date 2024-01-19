package tech.metavm.util;

public record ShoppingTypeIds(
        long productTypeId,
        long skuTypeId,
        long couponStateTypeId,
        long couponTypeId,
        long orderTypeId,
        long skuChildArrayTypeId,
        long productTitleFieldId,
        long productSkuListFieldId,
        long skuTitleFieldId,
        long skuPriceFieldId,
        long skuAmountFieldId,
        long couponTitleFieldId,
        long couponDiscountFieldId,
        long couponStateFieldId,
        long orderCodeFieldId,
        long orderProductFieldId,
        long orderAmountFieldId,
        long orderPriceFieldId,
        long orderTimeFieldId,
        long orderCouponsFieldId,
        long couponNormalStateId,
        long couponUsedStateId
) {
}
