package tech.metavm.autograph;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class QnResolver extends JavaRecursiveElementVisitor {

    @Override
    public void visitElement(@NotNull PsiElement element) {
        QnFactory.getQn(element);
        super.visitElement(element);
    }

}
