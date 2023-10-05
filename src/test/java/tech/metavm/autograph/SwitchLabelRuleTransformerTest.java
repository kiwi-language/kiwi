package tech.metavm.autograph;

import junit.framework.TestCase;
import tech.metavm.autograph.mocks.ColonSwitchFoo;

public class SwitchLabelRuleTransformerTest extends TestCase {

    public void test() {
        var foo = TranspileTestTools.getPsiJavaFile(ColonSwitchFoo.class);
        TranspileTestTools.executeCommand(
                () -> foo.accept(new SwitchLabelStatementTransformer())
        );
        System.out.println(foo.getText());
    }

}