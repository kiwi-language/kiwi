package org.metavm.autograph;

import junit.framework.TestCase;
import org.metavm.autograph.mocks.AstSwitchCoverFoo;

public class DefaultSwitchCaseAppenderTest extends TestCase {

    public void test() {
        var file = TranspileTestTools.getPsiJavaFile(AstSwitchCoverFoo.class);
        TranspileTestTools.executeCommand(() -> {
            file.accept(new SwitchExpressionTransformer());
            file.accept(new SwitchLabelStatementTransformer());
            file.accept(new DefaultSwitchCaseAppender());
        });
        System.out.println(file.getText());
    }

}