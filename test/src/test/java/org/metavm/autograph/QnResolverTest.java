package org.metavm.autograph;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiElement;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;

public class QnResolverTest extends TestCase {

    public void test() {
        var psiFile = TranspileTestTools.getPsiJavaFileByName("org.metavm.autograph.mocks.QnFoo");
        psiFile.accept(new QnResolver());
        psiFile.accept(new QnPrinter());
    }

    private static class QnPrinter extends JavaRecursiveElementVisitor {

        @Override
        public void visitElement(@NotNull PsiElement element) {
            super.visitElement(element);
            var qn = element.getUserData(Keys.QN_AND_MODE);
            if(qn != null) {
                System.out.println(element +  " " + qn);
            }
        }
    }


}