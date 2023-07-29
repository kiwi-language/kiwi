package tech.metavm.autograph;

import java.util.Objects;

public class SubscriptQualifiedName extends CompositeQualifiedName {

    private final int index;

    public SubscriptQualifiedName(QualifiedName parent, int index) {
        super(parent);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return parent.toString() + "[" + index + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubscriptQualifiedName that)) return false;
        return Objects.equals(parent, that.parent) && index == that.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, index);
    }
}
