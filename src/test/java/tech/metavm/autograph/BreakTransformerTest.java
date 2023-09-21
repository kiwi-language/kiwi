package tech.metavm.autograph;

import junit.framework.TestCase;
import tech.metavm.autograph.mocks.BreakFoo;

public class BreakTransformerTest extends TestCase {

    public void test() {
        var file = TranspileTestTools.getPsiJavaFile(BreakFoo.class);
        TranspileTestTools.executeCommand(() -> {
            file.accept(new BreakTransformer());
            System.out.println(file.getText());
            file.accept(new ContinueTransformer());
            System.out.println(file.getText());
        });

    }

}