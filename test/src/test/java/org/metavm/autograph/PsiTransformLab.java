package org.metavm.autograph;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiBreakStatement;
import com.intellij.psi.PsiForeachStatement;

public class PsiTransformLab {

    public static void main(String[] args) {
        var file = TranspileTestTools.getPsiJavaFileByName("org.metavm.autograph.mocks.TransformFoo");
        file.accept(new Transformer());
        System.out.println(file);
    }

    private static class Transformer extends JavaRecursiveElementVisitor {

        @Override
        public void visitForeachStatement(PsiForeachStatement statement) {
        }

        @Override
        public void visitBreakStatement(PsiBreakStatement statement) {
            statement.delete();
        }
    }

}
