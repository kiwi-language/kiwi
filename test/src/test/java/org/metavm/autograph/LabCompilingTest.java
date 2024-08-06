package org.metavm.autograph;

import org.junit.Assert;

public class LabCompilingTest extends CompilerTestBase {

    public static final String LAB_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/lab";
    public static final String LAB2_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/lab2";
    public static final String LAB3_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/lab3";

    public void test() {
        compile(LAB_SOURCE_ROOT);
        try {
            compile(LAB2_SOURCE_ROOT);
            Assert.fail("Should have failed");
        }
        catch (Exception ignored) {}
        compile(LAB3_SOURCE_ROOT);
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
