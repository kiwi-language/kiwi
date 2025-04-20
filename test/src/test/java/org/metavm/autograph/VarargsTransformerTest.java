package org.metavm.autograph;

import junit.framework.TestCase;

public class VarargsTransformerTest extends TestCase {

    public void test() {
        var file = TranspileTestTools.getPsiJavaFileByName("org.metavm.autograph.mocks.VarargsFoo");
        TranspileTestTools.executeCommand(() -> file.accept(new VarargsTransformer()));
        System.out.println(file.getText());
    }

}