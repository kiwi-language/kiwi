package tech.metavm.autograph;

import tech.metavm.util.NncUtils;

import java.util.*;

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
        Set<QualifiedName> set = new HashSet<>();
        set.add(rawTypeName);
        set.addAll(typeArgumentNames);
        return set;
    }
}
