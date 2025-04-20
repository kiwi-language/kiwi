package org.metavm.autograph;

import junit.framework.TestCase;

public class ForTransformerTest extends TestCase {

    public void test() {
        var file = TranspileTestTools.getPsiJavaFileByName("org.metavm.autograph.mocks.FooLoopFoo");
        TranspileTestTools.executeCommand(() -> {
            file.accept(new QnResolver());
            file.accept(new ActivityAnalyzer());
            file.accept(new ForTransformer());
        });
        System.out.println(file.getText());
    }

}