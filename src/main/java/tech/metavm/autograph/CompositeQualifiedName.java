package tech.metavm.autograph;

import java.util.Set;

public abstract class CompositeQualifiedName extends QualifiedName {

    protected final QualifiedName parent;

    public CompositeQualifiedName(QualifiedName parent) {
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
