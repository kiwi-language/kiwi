package org.metavm.autograph;

import junit.framework.TestCase;
import org.metavm.autograph.mocks.RecordFoo;

public class RecordTransformerTest extends TestCase {

    public void test() {
        var file = TranspileTestTools.getPsiJavaFile(RecordFoo.class);
        TranspileTestTools.executeCommand(() -> file.accept(new RecordTransformer()));
        System.out.println(file.getText());
    }

}