package org.metavm.autograph;

import junit.framework.TestCase;
import org.metavm.autograph.mocks.BreakFoo;

public class BreakTransformerTest extends TestCase {

    public void test() {
        var file = TranspileTestTools.getPsiJavaFile(BreakFoo.class);
        TranspileTestTools.executeCommand(() -> {
            file.accept(new QnResolver());
            file.accept(new ActivityAnalyzer());
            file.accept(new ForTransformer());
            file.accept(new QnResolver());
            file.accept(new ActivityAnalyzer());
            file.accept(new BreakTransformer());
            System.out.println(file.getText());
            file.accept(new ContinueTransformer());
            System.out.println(file.getText());
        });

    }

}