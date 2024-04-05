package tech.metavm.autograph;

import tech.metavm.util.DebugEnv;

public class CapturedTypesCompileTest extends CompilerTestBase {

    public static final String SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/capturedtypes";

    public void test() {
        compile(SOURCE_ROOT);
        DebugEnv.DEBUG_LOG_ON = true;
        compile(SOURCE_ROOT);
    }

}
