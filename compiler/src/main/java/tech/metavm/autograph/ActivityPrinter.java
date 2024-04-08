package tech.metavm.autograph;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActivityPrinter extends JavaRecursiveElementVisitor {

    public static final Logger DEBUG_LOGGER = LoggerFactory.getLogger("Debug");

    @Override
    public void visitElement(@NotNull PsiElement element) {
        var scope = element.getUserData(Keys.SCOPE);
        if (scope != null)
            DEBUG_LOGGER.info("Scope for node {}: {}", element.getText(), scope);
        super.visitElement(element);
    }
}
