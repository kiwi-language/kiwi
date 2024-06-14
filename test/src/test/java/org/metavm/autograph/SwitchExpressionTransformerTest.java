package org.metavm.autograph;

import junit.framework.TestCase;
import org.metavm.autograph.mocks.AstSwitchFoo;

public class SwitchExpressionTransformerTest extends TestCase {

    public void test() {
        var foo = TranspileTestTools.getPsiJavaFile(AstSwitchFoo.class);
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