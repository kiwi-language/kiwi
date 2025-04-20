package org.metavm.autograph;

import junit.framework.TestCase;

public class SwitchLabelRuleTransformerTest extends TestCase {

    public void test() {
        var foo = TranspileTestTools.getPsiJavaFileByName("org.metavm.autograph.mocks.ColonSwitchFoo");
        TranspileTestTools.executeCommand(
                () -> foo.accept(new SwitchLabelStatementTransformer())
        );
        System.out.println(foo.getText());
    }

}