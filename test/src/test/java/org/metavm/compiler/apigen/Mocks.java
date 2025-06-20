package org.metavm.compiler.apigen;

import org.metavm.compiler.element.*;
import org.metavm.compiler.type.PrimitiveType;
import org.metavm.compiler.type.Types;
import org.metavm.compiler.util.List;
import org.metavm.entity.AttributeNames;

public class Mocks {

    public static List<Clazz> createShoppingClasses() {
        var proj = new Project();
        return List.of(
                createCurrencyClass(proj),
                createMoneyClass(proj),
                createProductClass(proj),
                createOrderClass(proj),
                createOrderService(proj)
        );
    }

    private static Clazz createCurrencyClass(Project proj) {
        var cls = new Clazz(
                ClassTag.ENUM,
                "Currency",
                Access.PUBLIC,
                proj.getRootPackage()
        );
        new EnumConst(
                Name.from("CNY"),
                0,
                cls,
                cls
        );
        new EnumConst(
                Name.from("DOLLAR"),
                1,
                cls,
                cls
        );
        new EnumConst(
                Name.from("EURO"),
                2,
                cls,
                cls
        );
        return cls;
    }

    private static Clazz createMoneyClass(Project proj) {
        var cls = new Clazz(
                ClassTag.VALUE,
                "Money",
                Access.PUBLIC,
                proj.getRootPackage()
        );
        new Field(
                "amount",
                PrimitiveType.DOUBLE,
                Access.PUBLIC,
                false,
                cls
        );
        new Field(
                "currency",
                proj.getClass("Currency"),
                Access.PUBLIC,
                false,
                cls
        );
        return cls;
    }

    private static Clazz createProductClass(Project proj) {
        var cls = new Clazz(
                ClassTag.CLASS, Name.from("Product"),
                Access.PUBLIC,
                proj.getRootPackage()
        );
        new Field(
                "name",
                Types.instance.getStringType(),
                Access.PUBLIC,
                false,
                cls
        );
        new Field(
                "stock",
                PrimitiveType.INT,
                Access.PUBLIC,
                false,
                cls
        );
        new Field(
                "price",
                proj.getClass("Money"),
                Access.PUBLIC,
                false,
                cls
        );
        new Field(
                "desc",
                Types.instance.getNullableType(Types.instance.getStringType()),
                Access.PUBLIC,
                false,
                cls
        );

        var init = new Method(
                Name.init(),
                Access.PUBLIC,
                false,
                false,
                true,
                cls
        );
        new Param(
                "name",
                Types.instance.getStringType(),
                init
        );
        new Param(
                "stock",
                PrimitiveType.INT,
                init
        );
        new Param(
                "price",
                proj.getClass("Money"),
                init
        );
        new Param(
                "desc",
                Types.instance.getNullableType(Types.instance.getStringType()),
                init
        );
        var method = new Method(
                Name.from("reduceStock"),
                Access.PUBLIC,
                false,
                false,
                false,
                cls
        );
        new Param(Name.from("quantity"), PrimitiveType.INT, method);
        return cls;
    }

    private static Clazz createOrderClass(Project proj) {
        var cls = new Clazz(
                ClassTag.CLASS,
                "Order",
                Access.PUBLIC,
                proj.getRootPackage()
        );
        new Field(
                "price",
                proj.getClass("Money"),
                Access.PUBLIC,
                false,
                cls
        );
        var itemCls = new Clazz(
                ClassTag.CLASS,
                "Item",
                Access.PUBLIC,
                cls
        );
        new Field(
                "quantity",
                PrimitiveType.INT,
                Access.PUBLIC,
                false,
                itemCls
        );
        var productCls = proj.getRootPackage().getClass("Product");
        new Field(
                "product",
                productCls,
                Access.PUBLIC,
                false,
                itemCls
        );
        return cls;
    }

    private static Clazz createOrderService(Project proj) {
        var cls = new Clazz(
                ClassTag.CLASS,
                "OrderService",
                Access.PUBLIC,
                proj.getRootPackage()
        );
        cls.setAttributes(List.of(new Attribute(AttributeNames.BEAN_NAME, "orderService")));
        var placeOrderMeth = new Method(
                "placeOrder",
                Access.PUBLIC,
                false,
                false,
                false,
                cls
        );
        new Param(
                "products",
                Types.instance.getArrayType(proj.getClass("Product")),
                placeOrderMeth
        );
        placeOrderMeth.setRetType(proj.getClass("Order"));
        return cls;
    }

}
