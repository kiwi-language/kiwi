package org.metavm.autograph;

import junit.framework.TestCase;

public class SwitchExpressionTransformerTest extends TestCase {

    public void test() {
        var foo = TranspileTestTools.getPsiJavaFileByName("org.metavm.autograph.mocks.AstSwitchFoo");
        TranspileTestTools.executeCommand(
                () -> {
                    foo.accept(new QnResolver());
                    foo.accept(new ActivityAnalyzer());
                    foo.accept(new SwitchExpressionTransformer());
                }
        );
        System.out.println(foo.getText());
    }


}