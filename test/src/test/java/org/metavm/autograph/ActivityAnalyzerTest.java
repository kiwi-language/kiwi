package org.metavm.autograph;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiElement;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import org.metavm.autograph.mocks.ScopeFoo;

public class ActivityAnalyzerTest extends TestCase {

    public void test() {
        var psiFile = TranspileTestTools.getPsiJavaFile(ScopeFoo.class);
        var qnResolver = new QnResolver();
        psiFile.accept(qnResolver);
        var analyzer = new ActivityAnalyzer();
        psiFile.accept(analyzer);
        psiFile.accept(new ScopePrinter());
    }

    private static class ScopePrinter extends JavaRecursiveElementVisitor {

        @Override
        public void visitElement(@NotNull PsiElement element) {
            Scope scope;
            if((scope = element.getUserData(Keys.SCOPE)) != null) {
                System.out.println(element);
                System.out.println(scope);
                System.out.println();
            }
            super.visitElement(element);
        }

    }

}