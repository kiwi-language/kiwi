package org.metavm.autograph;

import junit.framework.TestCase;

public class RecordTransformerTest extends TestCase {

    public void test() {
        var file = TranspileTestTools.getPsiJavaFileByName("org.metavm.autograph.mocks.RecordFoo");
        TranspileTestTools.executeCommand(() -> file.accept(new RecordTransformer()));
        System.out.println(file.getText());
    }

}