package tech.metavm.util;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;

public record ShoppingTypes(
        ClassType productType,
        ClassType skuType,
        ClassType couponType,
        ClassType orderType,
        ClassType couponStateType,
        ArrayType skuChildArrayType,
        ArrayType couponArrayType,
        Field productTitleField,
        Field productSkuListField,
        Field skuTitleField,
        Field skuPriceField,
        Field skuAmountField,
        Field couponTitleField,
        Field couponDiscountField,
        Field couponStateField,
        Field orderCodeField,
        Field orderProductField,
        Field orderCouponsField,
        Field orderAmountField,
        Field orderPriceField,
        Field orderTimeField,
        ClassInstance couponNormalState,
        ClassInstance couponUsedState
) {
}
