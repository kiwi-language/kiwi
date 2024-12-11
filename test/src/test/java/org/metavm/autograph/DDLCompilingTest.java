package org.metavm.autograph;

import org.junit.Assert;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.MetadataState;
import org.metavm.util.TestConstants;

import java.util.List;
import java.util.Map;

public class DDLCompilingTest extends CompilerTestBase {

    public static final String DDL_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/ddl";
    public static final String DDL2_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/ddl2";
    public static final String DDL3_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/ddl3";
    public static final String DDL4_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/ddl4";

    public void test() {
        compile(DDL_SOURCE_ROOT);
        var ref = new Object() {
          Id stateFieldId;
          Id stateKlassId;
          Id derivedInstanceId;
          String fooId;
        };
        submit(() -> {
            try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
                ref.stateKlassId = context.getKlassByQualifiedName("ProductState").getId();
                var productKlass = context.getKlassByQualifiedName("Product");
                ref.stateFieldId = productKlass.getFieldByName("state").getId();
                ref.derivedInstanceId = Id.parse(saveInstance("swapsuper.Derived",
                        Map.of("value1", 1, "value2", 2, "value3", 3)));
                var indexFooKlass = context.getKlassByQualifiedName("index.IndexFoo");
                Assert.assertEquals(1, indexFooKlass.getConstraints().size());
            }
            ref.fooId = saveInstance("index.IndexFoo", Map.of("name", "foo", "seq", 1));
        });
//        try {
//            compile(DDL2_SOURCE_ROOT);
//            Assert.fail("Should have failed");
//        }
//        catch (Exception ignored) {}
        compile(DDL3_SOURCE_ROOT);
        submit(() -> {
            try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
                var productKlass = context.getKlassByQualifiedName("Product");
                var descField = productKlass.getFieldByName("description");
                Assert.assertSame(MetadataState.REMOVED, descField.getState());
                var statusFieldId = productKlass.getFieldByName("status");
                Assert.assertEquals(ref.stateFieldId, statusFieldId.getId());
                var productStatusKlass = context.getKlassByQualifiedName("ProductStatus");
                Assert.assertEquals(ref.stateKlassId, productStatusKlass.getId());
                var currencyKlass = context.getKlassByQualifiedName("Currency");
                var rateMethod = currencyKlass.getMethodByName("__rate__");
                Assert.assertTrue(rateMethod.isPublic());
                var yuanId = typeManager.getEnumConstantId("Currency", "YUAN");
                var rate = callMethod(yuanId, "__rate__", List.of());
                Assert.assertEquals(0.14, rate);
                var errors = productKlass.getErrors();
                Assert.assertEquals(0, errors.size());
                Assert.assertEquals(2, callMethod(ref.derivedInstanceId.toString(), "getValue2", List.of()));
                var indexFooKlass = context.getKlassByQualifiedName("index.IndexFoo");
                Assert.assertEquals(1, indexFooKlass.getConstraints().size());
            }
            Assert.assertEquals(ref.fooId, callMethod("index.IndexFoo", "findBySeq", List.of(1)));
        });
        compile(DDL4_SOURCE_ROOT);
//        compile(DDL3_SOURCE_ROOT);
//        submit(() -> {
////            DebugEnv.debugLogger_ON = true;
//            var fooType = getClassTypeByCode("Foo");
//            var fooConstructorId = TestUtils.getMethodIdByCode(fooType, "Foo");
//            var foo = TestUtils.doInTransaction(() ->
//                flowExecutionService.execute(new FlowExecutionRequest(
//                        fooConstructorId,
//                        null,
//                        List.of()
//                ))
//            );
//            var fooTestMethodId = TestUtils.getMethodIdByCode(fooType, "test");
//            TestUtils.doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
//                    fooTestMethodId,
//                    foo.id(),
//                    List.of()
//            )));
//        });
    }

}
