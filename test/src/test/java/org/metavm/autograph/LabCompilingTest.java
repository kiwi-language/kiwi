package org.metavm.autograph;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class LabCompilingTest extends CompilerTestBase {

    public static final String LAB_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/lab";

    public void test() {
        compile(LAB_SOURCE_ROOT);
        submit(this::processEmptyMethod);
    }

    private void processEmptyMethod() {
        var klassName = "misc.EmptyMethodFoo";
        callMethod(klassName, "test", List.of());
    }

}
