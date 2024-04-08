package tech.metavm.autograph;

import com.intellij.psi.PsiElement;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CfgNode {

    private final PsiElement element;
    private final List<CfgNode> next = new ArrayList<>();
    private final List<CfgNode> prev = new ArrayList<>();
    private final Set<CfgNode> backTargets = new LinkedHashSet<>();

    public CfgNode(PsiElement statement) {
        this.element = statement;
    }

    void addNext(CfgNode node, boolean isBackEdge) {
        next.add(node);
        node.prev.add(this);
        if(isBackEdge) backTargets.add(node);
    }

    public List<CfgNode> getNext() {
        return next;
    }

    public Set<CfgNode> getBackTargets() {
        return backTargets;
    }

    public boolean isBackTarget(CfgNode node) {
        return backTargets.contains(node);
    }

    public List<CfgNode> getPrev() {
        return prev;
    }

    public PsiElement getElement() {
        return element;
    }

    @Override
    public String toString() {
        return element.getClass().getSimpleName() + ":" + element.getText();
    }
}
