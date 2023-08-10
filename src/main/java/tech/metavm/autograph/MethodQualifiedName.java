package tech.metavm.autograph;

import tech.metavm.util.NncUtils;

import java.util.*;

public class MethodQualifiedName extends QualifiedName {

    private final QualifiedName className;
    private final String name;
    private final List<QualifiedName> parameterNames;

    public MethodQualifiedName(QualifiedName className, String name, List<QualifiedName> parameterNames) {
        this.className = className;
        this.name = name;
        this.parameterNames = new ArrayList<>(parameterNames);
    }

    public QualifiedName getClassName() {
        return className;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodQualifiedName that)) return false;
        return Objects.equals(className, that.className) && Objects.equals(name, that.name) && Objects.equals(parameterNames, that.parameterNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, name, parameterNames);
    }

    @Override
    public String toString() {
        return className + "." + name + "(" + NncUtils.join(parameterNames, Objects::toString, ",") + ")";
    }

    public List<QualifiedName> getParameterNames() {
        return Collections.unmodifiableList(parameterNames);
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
        set.add(className);
        set.addAll(parameterNames);
        return set;
    }
}
