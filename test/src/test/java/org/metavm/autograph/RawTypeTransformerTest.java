package org.metavm.autograph;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RawTypeTransformerTest extends TestCase {

    public static final String source = "/Users/leen/workspace/object/lab/src/main/basics/instanceof_/InstanceOfFoo.java";

    public void test() {
        var file = TranspileTestTools.getPsiJavaFile(source);
        TranspileTestTools.executeCommand(() -> file.accept(new RawTypeTransformer()));
        log.info("{}", file.getText());
    }

}