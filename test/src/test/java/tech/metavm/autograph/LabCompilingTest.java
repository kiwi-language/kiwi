package tech.metavm.autograph;

import tech.metavm.flow.rest.FlowExecutionRequest;
import tech.metavm.util.DebugEnv;
import tech.metavm.util.TestUtils;

import java.util.List;

public class LabCompilingTest extends CompilerTestBase {

    public static final String LAB_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/lab";

    public void test() {
//        compile(LAB_SOURCE_ROOT);
//        submit(() -> {
////            DebugEnv.DEBUG_LOG_ON = true;
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
