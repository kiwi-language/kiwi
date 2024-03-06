package tech.metavm.autograph;

import junit.framework.TestCase;
import tech.metavm.autograph.mocks.FooLoopFoo;

public class ForTransformerTest extends TestCase {

    public void test() {
        var file = TranspileTestTools.getPsiJavaFile(FooLoopFoo.class);
        TranspileTestTools.executeCommand(() -> {
            file.accept(new QnResolver());
            file.accept(new ActivityAnalyzer());
            file.accept(new ForTransformer());
        });
        System.out.println(file.getText());
    }

}