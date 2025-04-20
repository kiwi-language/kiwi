package org.metavm.autograph;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.common.ErrorCode;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.MetadataState;
import org.metavm.object.type.StaticFieldTable;
import org.metavm.util.BusinessException;
import org.metavm.util.Instances;
import org.metavm.util.TestConstants;
import org.metavm.util.TestUtils;

import java.util.List;
import java.util.Map;

@Slf4j
public class DDLCompilingTest extends CompilerTestBase {

    public static final String DDL_SOURCE_ROOT = "ddl";
    public static final String DDL2_SOURCE_ROOT = "ddl2";

    public void test() {
        compile(DDL_SOURCE_ROOT);
        var ref = new Object() {
          Id stateFieldId;
          Id stateKlassId;
          Id derivedInstanceId;
          String fooId;
          String productId;
          Id krwCurrencyId;
        };
        submit(() -> {
            try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
                context.loadKlasses();
                ref.stateKlassId = context.getKlassByQualifiedName("ProductState").getId();
                var productKlass = context.getKlassByQualifiedName("Product");
                ref.stateFieldId = productKlass.getFieldByName("state").getId();
                ref.derivedInstanceId = Id.parse(saveInstance("swapsuper.Derived",
                        Map.of("value1", 1, "value2", 2, "value3", 3)));
                var currencyKlass = context.getKlassByQualifiedName("Currency");
                ref.krwCurrencyId = StaticFieldTable.getInstance(currencyKlass.getType(), context).getEnumConstantByName("KRW").getId();
                var indexFooKlass = context.getKlassByQualifiedName("index.IndexFoo");
                Assert.assertEquals(1, indexFooKlass.getIndices().size());
            }
            ref.fooId = saveInstance("index.IndexFoo", Map.of("name", "foo", "seq", 1));
            ref.productId = saveInstance("Product", Map.of("name", "Shoes",
                    "price", Map.of(
                            "amount", 20,
                            "currency", "DOLLAR"
                    )
            ));
        });
        compile(DDL2_SOURCE_ROOT);
        submit(() -> {
            try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
                context.loadKlasses();
                var productKlass = context.getKlassByQualifiedName("Product");
                var descField = productKlass.getFieldByName("description");
                Assert.assertSame(MetadataState.REMOVED, descField.getState());
                var statusFieldId = productKlass.getFieldByName("status");
                Assert.assertEquals(ref.stateFieldId, statusFieldId.getId());
                var productStatusKlass = context.getKlassByQualifiedName("ProductStatus");
                Assert.assertEquals(ref.stateKlassId, productStatusKlass.getId());
                var currencyKlass = context.getKlassByQualifiedName("Currency");
                var yuan = StaticFieldTable.getInstance(currencyKlass.getType(), context).getEnumConstantByName("YUAN");
                Assert.assertEquals(
                        Instances.doubleInstance(0.14), yuan.getField("rate")
                );
                var rateMethod = currencyKlass.getMethodByName("__rate__");
                Assert.assertTrue(rateMethod.isPublic());
                var yuanId = typeManager.getEnumConstantId("Currency", "YUAN");
                var rate = callMethod(yuanId, "__rate__", List.of());
                Assert.assertEquals(0.14, rate);
                var errors = productKlass.getErrors();
                Assert.assertEquals(0, errors.size());
                Assert.assertEquals(2, callMethod(ref.derivedInstanceId.toString(), "getValue2", List.of()));
                var indexFooKlass = context.getKlassByQualifiedName("index.IndexFoo");
                Assert.assertEquals(1, indexFooKlass.getIndices().size());
                Assert.assertEquals(4, currencyKlass.getEnumConstants().size());
                int ordinal = 0;
                for (var enumConstantDef : currencyKlass.getEnumConstants()) {
                    Assert.assertEquals(ordinal++, enumConstantDef.getOrdinal());
                }
            }
            Assert.assertEquals(ref.fooId, callMethod("index.IndexFoo", "findBySeq", List.of(1)));
            Assert.assertEquals("AVAILABLE", getStatic("Product", "DEFAULT_STATUS"));
            Assert.assertEquals("EURO", getStatic("Currency", "EURO"));
            var product = getObject(ref.productId);
            Assert.assertEquals("none", product.getString("tag"));
            Assert.assertEquals("USD", product.getObject("price").getString("currency"));
//            try {
//                getObject(ref.krwCurrencyId.toString());
//                Assert.fail("Enum constant should have been removed");
//            }
//            catch (BusinessException e) {
//                Assert.assertSame(ErrorCode.INSTANCE_NOT_FOUND, e.getErrorCode());
//            }
        });
    }

}
