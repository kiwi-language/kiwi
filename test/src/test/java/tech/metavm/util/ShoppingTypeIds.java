package tech.metavm.util;

public record ShoppingTypeIds(
        long productTypeId,
        long skuTypeId,
        long couponStateTypeId,
        long couponTypeId,
        long orderTypeId,
        long skuChildArrayTypeId,
        long couponArrayTypeId,
        long productTitleFieldId,
        long productSkuListFieldId,
        long skuTitleFieldId,
        long skuPriceFieldId,
        long skuAmountFieldId,
        long skuDecAmountMethodId,
        long skuBuyMethodId,
        long couponTitleFieldId,
        long couponDiscountFieldId,
        long couponStateFieldId,
        long orderCodeFieldId,
        long orderSkuFieldId,
        long orderAmountFieldId,
        long orderPriceFieldId,
        long orderTimeFieldId,
        long orderCouponsFieldId,
        String couponNormalStateId,
        String couponUsedStateId
) {
}
