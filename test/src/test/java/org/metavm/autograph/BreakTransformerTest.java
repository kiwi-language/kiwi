package org.metavm.autograph;

import com.intellij.psi.PsiJavaFile;
import junit.framework.TestCase;

public class BreakTransformerTest extends TestCase {

    public void test() {
        var file = TranspileTestTools.getPsiJavaFileByName("org.metavm.autograph.mocks.BreakFoo");
        transform(file);
    }

    private void transform(PsiJavaFile file) {
        TranspileTestTools.executeCommand(() -> {
            file.accept(new BodyNormalizer());
            file.accept(new QnResolver());
            file.accept(new ActivityAnalyzer());
            file.accept(new ForTransformer());
            file.accept(new QnResolver());
            file.accept(new ActivityAnalyzer());
            file.accept(new BreakTransformer());
            System.out.println(file.getText());
            file.accept(new QnResolver());
            file.accept(new ActivityAnalyzer());
            file.accept(new ContinueTransformer());
            System.out.println(file.getText());
        });
    }
}