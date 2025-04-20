package org.metavm.autograph;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiJavaParserFacade;
import com.intellij.psi.PsiMethod;

import static org.metavm.autograph.TranspileTestTools.getProject;

public class PsiModLab {

    public static void main(String[] args) {
        var psiFile = TranspileTestTools.getPsiJavaFileByName("org.metavm.autograph.mocks.ModFoo", true);
        psiFile.accept(new ModVisitor());
    }

    private static class ModVisitor extends JavaRecursiveElementVisitor {

        @Override
        public void visitMethod(PsiMethod method) {
            PsiJavaParserFacade parserFacade = JavaPsiFacade.getInstance(getProject()).getParserFacade();
            var comment = parserFacade.createCommentFromText("// Comment added by PsiModLab", null);
            method.getParent().addBefore(comment, method);
        }
    }

}
