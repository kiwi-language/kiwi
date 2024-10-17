package org.metavm.autograph;

import org.junit.Assert;
import org.metavm.flow.rest.MethodParam;
import org.metavm.object.type.Access;
import org.metavm.object.type.MetadataState;
import org.metavm.util.NncUtils;
import org.metavm.util.TestUtils;

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
          String stateFieldId;
          String stateKlassId;
          String derivedInstanceId;
        };
        submit(() -> {
            ref.stateKlassId = getClassTypeByCode("ProductState").id();
            var productKlass = getClassTypeByCode("Product");
            ref.stateFieldId = TestUtils.getFieldIdByCode(productKlass, "state");
            ref.derivedInstanceId = saveInstance("swapsuper.Derived",
                    Map.of("value1", 1, "value2", 2, "value3", 3));
        });
//        try {
//            compile(DDL2_SOURCE_ROOT);
//            Assert.fail("Should have failed");
//        }
//        catch (Exception ignored) {}
        compile(DDL3_SOURCE_ROOT);
        submit(() -> {
            var productKlass = getClassTypeByCode("Product");
            var descField = TestUtils.getFieldByName(productKlass, "description");
            Assert.assertEquals(MetadataState.REMOVED.code(), descField.state());
            var statusFieldId = TestUtils.getFieldIdByCode(productKlass, "status");
            Assert.assertEquals(ref.stateFieldId, statusFieldId);
            var productStatusKlass = getClassTypeByCode("ProductStatus");
            Assert.assertEquals(ref.stateKlassId, productStatusKlass.id());
            var currencyKlass = getClassTypeByCode("Currency");
            var rateMethod = NncUtils.findRequired(currencyKlass.flows(), m -> m.name().equals("__rate__"));
            Assert.assertEquals(Access.PUBLIC.code(), ((MethodParam)rateMethod.param()).access());
            var yuanId = typeManager.getEnumConstant(currencyKlass.id(), "YUAN").id();
            var rate = TestUtils.doInTransaction(() -> apiClient.callMethod(yuanId, "__rate__", List.of()));
            Assert.assertEquals(0.14, rate);
            var errors = productKlass.errors();
            Assert.assertEquals(0, errors.size());
            Assert.assertEquals(2L, callMethod(ref.derivedInstanceId, "getValue2", List.of()));
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
