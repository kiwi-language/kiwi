package org.metavm.autograph.env;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.codeStyle.IndentHelper;
import org.jetbrains.annotations.NotNull;

public class SimpleIndentHelper extends IndentHelper {
    @Override
    public int getIndent(@NotNull PsiFile file, @NotNull ASTNode element) {
        return 4;
    }

    @Override
    public int getIndent(@NotNull PsiFile file, @NotNull ASTNode element, boolean includeNonSpace) {
        return 4;
    }
}
