package org.metavm.autograph;

import com.intellij.psi.PsiElement;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.IdentitySet;

import java.util.Map;
import java.util.Set;

@Slf4j
public record Graph(
        String title,
        CfgNode entry,
        Set<CfgNode> exit,
        Set<PsiElement> errors,
        Map<PsiElement, CfgNode> nodeIndex,
        Map<PsiElement, Set<CfgNode>> stmtPrev,
        Map<PsiElement, Set<CfgNode>> stmtNext
) {

    public void log() {
        log.debug("Cfg for {}", title);
        logNodeDfs(entry, new IdentitySet<>());
    }

    private void logNodeDfs(CfgNode node, IdentitySet<CfgNode> visited) {
        if(visited.add(node)) {
            log.debug("{} -> {}", node, node.getNext());
            node.getNext().forEach(next -> logNodeDfs(next, visited));
        }
    }


}
