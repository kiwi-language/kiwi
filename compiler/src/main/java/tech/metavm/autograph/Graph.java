package tech.metavm.autograph;

import com.intellij.psi.PsiElement;

import java.util.Map;
import java.util.Set;

public record Graph(
        String title,
        CfgNode entry,
        Set<CfgNode> exit,
        Set<PsiElement> errors,
        Map<PsiElement, CfgNode> nodeIndex,
        Map<PsiElement, Set<CfgNode>> stmtPrev,
        Map<PsiElement, Set<CfgNode>> stmtNext
) {
}
