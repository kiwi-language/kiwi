package org.metavm.autograph;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.metavm.mocks.RecordToClassFoo;

@Slf4j
public class RecordToClassTest extends TestCase {

    public void test() {
        var file = TranspileTestTools.getPsiJavaFile(RecordToClassFoo.class);
        TranspileTestTools.executeCommand(() -> file.accept(new RecordToClass()));
        log.info("{}", file.getText());
    }

}