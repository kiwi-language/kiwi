package org.metavm.autograph;

import com.intellij.psi.PsiClass;

public class ClassState extends State {

    private PsiClass node;

    public ClassState(State parent, StateStack stack) {
        super(parent, stack);
    }

    public void setNode(PsiClass node) {
        this.node = node;
    }
}
