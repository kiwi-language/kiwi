package org.metavm.autograph;

import junit.framework.TestCase;

import java.io.IOException;

public class ContinueTransformerTest extends TestCase {

    public void test() throws IOException {
        var file = TranspileTestTools.getPsiJavaFileByName("org.metavm.autograph.mocks.ContinueFoo");
        TranspileTestTools.executeCommand(
                () -> {
                    file.accept(new QnResolver());
                    file.accept(new ActivityAnalyzer());
                    file.accept(new ForTransformer());
                    file.accept(new QnResolver());
                    file.accept(new ActivityAnalyzer());
                    file.accept(new BreakTransformer());
                    file.accept(new QnResolver());
                    file.accept(new ActivityAnalyzer());
                    file.accept(new ContinueTransformer());
                }
        );
        System.out.println(file.getText());
    }


}