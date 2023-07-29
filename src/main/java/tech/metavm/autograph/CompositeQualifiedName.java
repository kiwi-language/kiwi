package tech.metavm.autograph;

public abstract class CompositeQualifiedName extends QualifiedName {

    protected final QualifiedName parent;

    public CompositeQualifiedName(QualifiedName parent) {
        this.parent = parent;
    }
}
