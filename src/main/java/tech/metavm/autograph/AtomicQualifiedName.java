package tech.metavm.autograph;

import com.intellij.psi.PsiType;

import java.util.Objects;
import java.util.Set;

public class AtomicQualifiedName extends QualifiedName {
    private final String name;

    public AtomicQualifiedName(String name, PsiType type) {
        super(type);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AtomicQualifiedName that)) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public boolean isComposite() {
        return false;
    }

    @Override
    public Set<QualifiedName> supportSet() {
        return Set.of();
    }

}
