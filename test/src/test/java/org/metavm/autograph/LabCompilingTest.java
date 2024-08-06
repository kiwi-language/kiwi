package org.metavm.autograph;

import org.junit.Assert;
import org.metavm.object.type.MetadataState;
import org.metavm.util.TestUtils;

public class LabCompilingTest extends CompilerTestBase {

    public static final String LAB_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/lab";
    public static final String LAB2_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/lab2";
    public static final String LAB3_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/lab3";

    public void test() {
        compile(LAB_SOURCE_ROOT);
        var ref = new Object() {
          String stateFieldId;
          String stateKlassId;
        };
        submit(() -> {
            ref.stateKlassId = getClassTypeByCode("ProductState").id();
            var productKlass = getClassTypeByCode("Product");
            ref.stateFieldId = TestUtils.getFieldIdByCode(productKlass, "state");
        });
        try {
            compile(LAB2_SOURCE_ROOT);
            Assert.fail("Should have failed");
        }
        catch (Exception ignored) {}
        compile(LAB3_SOURCE_ROOT);
        submit(() -> {
            var productKlass = getClassTypeByCode("Product");
            var descField = TestUtils.getFieldByName(productKlass, "description");
            Assert.assertEquals(MetadataState.REMOVED.code(), descField.state());
            var statusFieldId = TestUtils.getFieldIdByCode(productKlass, "status");
            Assert.assertEquals(ref.stateFieldId, statusFieldId);
            var productStatusKlass = getClassTypeByCode("ProductStatus");
            Assert.assertEquals(ref.stateKlassId, productStatusKlass.id());
        });
//        compile(LAB3_SOURCE_ROOT);
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
