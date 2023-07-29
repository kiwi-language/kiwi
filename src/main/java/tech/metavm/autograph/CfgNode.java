package tech.metavm.autograph;

import com.intellij.psi.PsiElement;

import java.util.ArrayList;
import java.util.List;

public class CfgNode {

    private final PsiElement element;
    private final List<CfgNode> next = new ArrayList<>();
    private final List<CfgNode> prev = new ArrayList<>();

    public CfgNode(PsiElement statement) {
        this.element = statement;
    }

    void addNext(CfgNode node) {
        next.add(node);
        node.prev.add(this);
    }

    public List<CfgNode> getNext() {
        return next;
    }

    public List<CfgNode> getPrev() {
        return prev;
    }

    public PsiElement getElement() {
        return element;
    }
}
