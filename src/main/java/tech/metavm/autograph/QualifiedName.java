package tech.metavm.autograph;

import java.util.Set;

public abstract class QualifiedName {

    public abstract boolean isSimple();

    public abstract boolean isComposite();

    public abstract Set<QualifiedName> supportSet();

}
