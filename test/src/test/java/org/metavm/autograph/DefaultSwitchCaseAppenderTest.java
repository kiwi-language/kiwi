package org.metavm.autograph;

import junit.framework.TestCase;

public class DefaultSwitchCaseAppenderTest extends TestCase {

    public void test() {
        var file = TranspileTestTools.getPsiJavaFileByName("org.metavm.autograph.mocks.AstSwitchCoverFoo");
        TranspileTestTools.executeCommand(() -> {
            file.accept(new QnResolver());
            file.accept(new ActivityAnalyzer());
            file.accept(new SwitchExpressionTransformer());
            file.accept(new SwitchLabelStatementTransformer());
            file.accept(new DefaultSwitchCaseAppender());
        });
        System.out.println(file.getText());
    }

}