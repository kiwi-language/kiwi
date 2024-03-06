package tech.metavm.autograph;

import tech.metavm.util.NncUtils;

import java.util.*;

public class Scope {

    private final Set<QualifiedName> modified = new HashSet<>();
    private final Set<QualifiedName> defined = new HashSet<>();
    private final Set<QualifiedName> read = new HashSet<>();
    private final Set<QualifiedName> isolatedNames = new HashSet<>();
    private final Scope parent;
    private final String methodName;
    private final List<Scope> children = new ArrayList<>();

    public Scope(Scope parent, String methodName) {
        this.parent = parent;
        this.methodName = methodName;
    }

    void addModified(QualifiedName qualifiedName) {
        modified.add(qualifiedName);
    }

    void addIsolatedName(QualifiedName isolatedName) {
        isolatedNames.add(isolatedName);
    }

    void addRead(QualifiedName qualifiedName) {
        read.add(qualifiedName);
    }

    void addDefined(QualifiedName qualifiedName) {
        defined.add(qualifiedName);
    }

    public String getMethodName() {
        return methodName;
    }

    public Set<QualifiedName> getDefined() {
        return Collections.unmodifiableSet(defined);
    }

    public Set<QualifiedName> getModified() {
        return Collections.unmodifiableSet(modified);
    }

    public Set<QualifiedName> getRead() {
        return Collections.unmodifiableSet(read);
    }

    public Scope getParent() {
        return parent;
    }

    public static Scope copyOf(Scope scope) {
        return scope != null ? scope.copy() : new Scope(null, null);
    }

    public Scope copy() {
        var copy = new Scope(NncUtils.get(parent, Scope::copy), methodName);
        copy.copyFrom(this);
        return copy;
    }

    public void copyFrom(Scope from) {
        modified.clear();
        defined.clear();
        read.clear();
        if (from != null) mergeFrom(from);
    }

    public void mergeFrom(Scope from) {
        modified.addAll(from.modified);
        defined.addAll(from.defined);
        read.addAll(from.read);
    }


    public void complete() {
        if (parent != null) {
            // TODO: check correctness
            parent.defined.addAll(defined);
            parent.read.addAll(NncUtils.diffSet(read, isolatedNames));
            parent.modified.addAll(NncUtils.diffSet(modified, isolatedNames));
        }
    }

    public Set<QualifiedName> getAllDefined() {
        var defined = new HashSet<QualifiedName>();
        var p = this.parent;
        while (p != null) {
            defined.addAll(p.defined);
            p = p.parent;
        }
        getAllDefined(defined);
        return defined;
    }

    private void getAllDefined(Set<QualifiedName> defined) {
        defined.addAll(this.defined);
        children.forEach(child -> child.getAllDefined(defined));
    }

    @Override
    public String toString() {
        return "Scope{" +
                "modified=" + modified +
                ", defined=" + defined +
                ", read=" + read +
                ", methodName='" + methodName + '\'' +
                '}';
    }
}
