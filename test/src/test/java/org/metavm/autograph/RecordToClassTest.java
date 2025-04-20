package org.metavm.autograph;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RecordToClassTest extends TestCase {

    public void test() {
        var file = TranspileTestTools.getPsiJavaFileByName("org.metavm.mocks.RecordToClassFoo");
        TranspileTestTools.executeCommand(() -> file.accept(new RecordToClass()));
        log.info("{}", file.getText());
    }

}