package org.metavm.autograph;

import junit.framework.TestCase;
import org.metavm.autograph.mocks.AstForeachFoo;

public class ForeachTransformerTest extends TestCase {

    public void test() {
        var file = TranspileTestTools.getPsiJavaFile(AstForeachFoo.class);
        TranspileTestTools.executeCommand(() -> {
            file.accept(new QnResolver());
            file.accept(new ActivityAnalyzer());
            file.accept(new ForeachTransformer());
        });
        System.out.println(file.getText());
    }

}