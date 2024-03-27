package tech.metavm.autograph;

import org.junit.Assert;
import tech.metavm.flow.rest.FlowExecutionRequest;
import tech.metavm.object.instance.rest.PrimitiveFieldValue;
import tech.metavm.object.instance.rest.PrimitiveInstanceParam;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.util.NncUtils;
import tech.metavm.util.TestUtils;

import java.util.List;

public class LabCompilingTest extends CompilerTestBase {

    public static final String LAB_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/lab";

    public void test() {
        var classCodes = compile(LAB_SOURCE_ROOT);
        var ref = new Object() {
            List<TypeDTO> classes;
        };
        submit(() -> {
            ref.classes = NncUtils.map(classCodes, this::getClassTypeByCode);
        });
        processSwitchExpression();
        var recompiledClassCodes = compile(LAB_SOURCE_ROOT);
        submit(() -> {
            var recompiledClasses = NncUtils.map(recompiledClassCodes, this::getClassTypeByCode);
            Assert.assertEquals(classCodes, recompiledClassCodes);
            ref.classes.forEach(clazz -> {
                var recompiledClass = recompiledClasses.get(ref.classes.indexOf(clazz));
                Assert.assertEquals(clazz, recompiledClass);
            });
        });
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
