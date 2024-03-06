package tech.metavm.autograph;

import tech.metavm.flow.rest.FlowExecutionRequest;
import tech.metavm.object.instance.rest.PrimitiveFieldValue;
import tech.metavm.object.instance.rest.PrimitiveInstanceParam;
import tech.metavm.util.TestUtils;

import java.util.List;

public class LabCompilingTest extends CompilerTestBase {

    public static final String LAB_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/lab";

    public void test() {
        compile(LAB_SOURCE_ROOT);
        processSwitchExpression();
    }

    private void processSwitchExpression() {
        submit(() -> {
            var switchExpressionLab = getClassTypeByCode("SwitchExpressionLab");
            var testMethodId = TestUtils.getMethodIdByCode(switchExpressionLab, "getNumber");
            var result = TestUtils.doInTransaction(() ->
                    flowExecutionService.execute(new FlowExecutionRequest(
                            testMethodId,
                            null,
                            List.of(
                                    PrimitiveFieldValue.createLong(4)
                            )
                    ))
            );
            assertEquals("tens", ((PrimitiveInstanceParam) result.param()).value());
        });
    }


}
