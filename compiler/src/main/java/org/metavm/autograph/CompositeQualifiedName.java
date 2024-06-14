package org.metavm.autograph;

import com.intellij.psi.PsiType;

import java.util.Set;

public abstract class CompositeQualifiedName extends QualifiedName {

    protected final QualifiedName parent;

    public CompositeQualifiedName(QualifiedName parent, PsiType type) {
        super(type);
        this.parent = parent;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public boolean isComposite() {
        return true;
    }

    @Override
    public Set<QualifiedName> supportSet() {
        return Set.of(parent);
    }
}
