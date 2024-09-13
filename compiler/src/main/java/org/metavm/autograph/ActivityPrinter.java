package org.metavm.autograph;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActivityPrinter extends SkipDiscardedVisitor {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    @Override
    public void visitElement(@NotNull PsiElement element) {
        var scope = element.getUserData(Keys.SCOPE);
        if (scope != null)
            debugLogger.info("Scope for node {}: {}", element.getText(), scope);
        super.visitElement(element);
    }
}
