package tech.metavm.autograph;

import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PTypeQualifiedName extends QualifiedName {

    private final QualifiedName rawTypeName;
    private final List<QualifiedName> typeArgumentNames;

    public PTypeQualifiedName(QualifiedName rawTypeName, List<QualifiedName> typeArgumentNames) {
        this.rawTypeName = rawTypeName;
        this.typeArgumentNames = new ArrayList<>(typeArgumentNames);
    }

    public List<QualifiedName> getTypeArgumentNames() {
        return Collections.unmodifiableList(typeArgumentNames);
    }

    public QualifiedName getRawTypeName() {
        return rawTypeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PTypeQualifiedName that)) return false;
        return Objects.equals(rawTypeName, that.rawTypeName) && Objects.equals(typeArgumentNames, that.typeArgumentNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawTypeName, typeArgumentNames);
    }

    @Override
    public String toString() {
        return rawTypeName + "<" + NncUtils.join(typeArgumentNames, Objects::toString) + ">";
    }
}
