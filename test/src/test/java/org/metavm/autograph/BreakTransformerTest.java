package org.metavm.autograph;

import com.intellij.psi.PsiJavaFile;
import junit.framework.TestCase;
import org.metavm.autograph.mocks.BreakFoo;
import org.metavm.util.Utils;

public class BreakTransformerTest extends TestCase {

    public void test() {
        var file = TranspileTestTools.getPsiJavaFile(BreakFoo.class);
        transform(file);
    }


    public void test2() {
        var source = "/Users/leen/workspace/object/lab/src/main/basics/break_/BreakFoo.java";
        var file = TranspileTestTools.getPsiJavaFile(source);
        transform(file);
        var output = "/Users/leen/workspace/object/lab/src/main/tmp/break2/BreakFoo.java";
        Utils.writeFile(output, file.getText().replace("package break_;", "package break2;"));
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