package tech.metavm.autograph;

import java.util.Objects;

public class AtomicQualifiedName extends QualifiedName {
    private final String name;

    public AtomicQualifiedName(String name) {
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
}
