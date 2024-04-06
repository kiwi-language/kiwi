package tech.metavm.autograph;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.ErrorDTO;
import tech.metavm.util.DebugEnv;

public class CapturedTypesCompileTest extends CompilerTestBase {

    public static final Logger LOGGER = LoggerFactory.getLogger(CapturedTypesCompileTest.class);

    public static final String SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/capturedtypes";

    public void test() {
//        compile(SOURCE_ROOT);
//        DebugEnv.DEBUG_LOG_ON = true;
//        compile(SOURCE_ROOT);
//        submit(() -> {
//            var utilsType = getClassTypeByCode("CtUtils");
//            for (ErrorDTO error : utilsType.getClassParam().errors()) {
//                LOGGER.info("Utils error: {}", error.message());
//            }
//            Assert.assertEquals(0, utilsType.getClassParam().errors().size());
//        });
    }

}
