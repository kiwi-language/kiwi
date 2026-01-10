package org.manul.util;

import org.manul.object.instance.core.ClassInstance;
import org.manul.object.type.ArrayType;
import org.manul.object.type.Field;
import org.manul.object.type.Klass;

public record ShoppingTypes(
        Klass productType,
        Klass skuType,
        Klass couponType,
        Klass orderType,
        Klass couponStateType,
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
