package tech.metavm.autograph;

import junit.framework.TestCase;
import tech.metavm.autograph.mocks.AstSwitchFoo;
import tech.metavm.autograph.mocks.SwitchExprFoo;

public class SwitchExpressionTransformerTest extends TestCase {

    public void test() {
        var foo = TranspileTestTools.getPsiJavaFile(AstSwitchFoo.class);
        TranspileTestTools.executeCommand(
                () -> foo.accept(new SwitchExpressionTransformer())
        );
        System.out.println(foo.getText());
    }


}