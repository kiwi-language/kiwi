package org.metavm.autograph;

import com.intellij.psi.PsiElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.metavm.util.IdentitySet;

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

    public static final Logger degugLogger = LoggerFactory.getLogger("Debug");

    public void log() {
        degugLogger.info("Cfg for {}", title);
        logNodeDfs(entry, new IdentitySet<>());
    }

    private void logNodeDfs(CfgNode node, IdentitySet<CfgNode> visited) {
        if(visited.add(node)) {
            degugLogger.info("{} -> {}", node, node.getNext());
            node.getNext().forEach(next -> logNodeDfs(next, visited));
        }
    }


}
