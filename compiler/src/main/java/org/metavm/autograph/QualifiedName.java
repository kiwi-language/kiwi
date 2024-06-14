package org.metavm.autograph;

import com.intellij.psi.PsiType;

import java.util.Set;

public abstract class QualifiedName {

    private final PsiType type;

    public QualifiedName(PsiType type) {
        this.type = type;
    }

    public abstract boolean isSimple();

    public abstract boolean isComposite();

    public abstract Set<QualifiedName> supportSet();

    public final PsiType type() {
        return type;
    }

}
