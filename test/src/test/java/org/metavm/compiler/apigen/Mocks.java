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
                createCouponClass(proj),
                createOrderStatusClass(proj),
                createOrderClass(proj),
                createOrderService(proj),
                createFileClass(proj)
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

    private static void createCanonicalInit(Clazz cls) {
        createInit(cls, cls.getFields().filter(f -> f.isPublic() && !f.isStatic()));
    }

    private static void createInit(Clazz cls, List<Field> fields) {
        var init = new Method(
                Name.init(),
                Access.PUBLIC,
                false,
                false,
                true,
                cls
        );
        for (Field field : fields) {
            if (!field.isStatic() && field.isPublic())
                new Param(field.getName(), field.getType(), init);
        }
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
        var summaryField = new Field(
                "summary",
                Types.instance.getStringType(),
                Access.PRIVATE,
                false,
                cls
        );
        cls.setSummaryField(summaryField);
        createCanonicalInit(cls);
        return cls;
    }

    private static Clazz createProductClass(Project proj) {
        var cls = new Clazz(
                ClassTag.CLASS, Name.from("Product"),
                Access.PUBLIC,
                proj.getRootPackage()
        );
        var nameField = new Field(
                "name",
                Types.instance.getStringType(),
                Access.PUBLIC,
                false,
                cls
        );
        cls.setSummaryField(nameField);
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
        createCanonicalInit(cls);
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

    private static Clazz createOrderStatusClass(Project proj) {
        var cls = new Clazz(
                ClassTag.ENUM,
                "OrderStatus",
                Access.PUBLIC,
                proj.getRootPackage()
        );
        new EnumConst(Name.from("PENDING"), 0, cls, cls);
        new EnumConst(Name.from("CONFIRMED"), 1, cls, cls);
        new EnumConst(Name.from("CANCELLED"), 2, cls, cls);
        return cls;
    }

    private static Clazz createOrderClass(Project proj) {
        var cls = new Clazz(
                ClassTag.CLASS,
                "Order",
                Access.PUBLIC,
                proj.getRootPackage()
        );
        var priceField = new Field(
                "price",
                proj.getClass("Money"),
                Access.PUBLIC,
                false,
                cls
        );
        new Field(
                "status",
                proj.getClass("OrderStatus"),
                Access.PUBLIC,
                false,
                cls
        );
        createInit(cls, List.of(priceField));
        var itemCls = new Clazz(
                ClassTag.CLASS,
                "OrderItem",
                Access.PUBLIC,
                cls
        );
        var quntityField = new Field(
                "quantity",
                PrimitiveType.INT,
                Access.PUBLIC,
                false,
                itemCls
        );
        var productField = new Field(
                "product",
                proj.getClass("Product"),
                Access.PUBLIC,
                false,
                itemCls
        );
        new Field(
                "productName",
                Types.instance.getStringType(),
                Access.PUBLIC,
                false,
                itemCls
        );
        createInit(itemCls, List.of(quntityField, productField));
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
        new Param(
                "coupon",
                Types.instance.getNullableType(proj.getClass("Coupon")),
                placeOrderMeth
        );
        placeOrderMeth.setRetType(proj.getClass("Order"));
        new Method(
                "cancelAllPendingOrders",
                Access.PUBLIC,
                false,
                false,
                false,
                cls
        );
        createCanonicalInit(cls);
        return cls;
    }

    private static Clazz createCouponClass(Project proj) {
        var cls = new Clazz(
                ClassTag.CLASS,
                "Coupon",
                Access.PUBLIC,
                proj.getRootPackage()
        );
        var title = new Field(
                "title",
                Types.instance.getStringType(),
                Access.PUBLIC,
                false,
                cls
        );
        var discount = new Field(
                "discount",
                PrimitiveType.DOUBLE,
                Access.PUBLIC,
                false,
                cls
        );
        var product = new Field(
                "product",
                proj.getClass("Product"),
                Access.PUBLIC,
                false,
                cls
        );
        new Field(
                "redeemed",
                PrimitiveType.BOOL,
                Access.PUBLIC,
                false,
                cls
        );
        createInit(cls, List.of(title, discount, product));
        new Method("redeem", Access.PUBLIC, false, false, false, cls);
        return cls;
    }

    private static Clazz createFileClass(Project proj) {
        var cls = ClazzBuilder.newBuilder(Name.from("File"), proj.getRootPackage()).build();
        var nameField = new Field(
                "name",
                Types.instance.getStringType(),
                Access.PUBLIC,
                false,
                cls
        );
        var urlField = new Field(
                "url",
                Types.instance.getStringType(),
                Access.PUBLIC,
                false,
                cls
        );
        createInit(cls, List.of(nameField, urlField));
        return cls;
    }

}
