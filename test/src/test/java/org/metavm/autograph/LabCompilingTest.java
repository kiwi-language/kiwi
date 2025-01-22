package org.metavm.autograph;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

@Slf4j
public class LabCompilingTest extends CompilerTestBase {

    public static final String LAB_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/lab";

    public void test() throws ExecutionException, InterruptedException {
        compile(LAB_SOURCE_ROOT);
        submit(() -> {});
    }

}
