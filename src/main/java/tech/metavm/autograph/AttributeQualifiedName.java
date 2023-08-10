package tech.metavm.autograph;

import java.util.Objects;

public class AttributeQualifiedName extends CompositeQualifiedName {

    private final String attributeName;

    public AttributeQualifiedName(QualifiedName parent, String attributeName) {
        super(parent);
        this.attributeName = attributeName;
    }

    public String getAttributeName() {
        return attributeName;
    }

    @Override
    public String toString() {
        return parent.toString() + "." + attributeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttributeQualifiedName that)) return false;
        return Objects.equals(parent, that.parent) && Objects.equals(attributeName, that.attributeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, attributeName);
    }

}

