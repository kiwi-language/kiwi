package org.metavm.compiler.element;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class Package implements ClassScope, Element {
    private Project project;
    private SymName name;
    private final @Nullable Package parent;

    private final Map<SymName, Clazz> classes = new HashMap<>();
    private final Map<SymName, FreeFunc> functions = new HashMap<>();
    private final Map<SymName, Package> packages = new HashMap<>();

    private final ElementTable table = new ElementTable();

    public Package(SymName name, @Nullable Package parent, Project project) {
        this.name = name;
        this.parent = parent;
        this.project = project;
        if (parent != null)
            parent.addPackage(this);
    }

    public SymName getName() {
        return name;
    }

    public void setName(SymName name) {
        this.name = name;
    }

    public SymName getQualifiedName() {
        if (parent != null) {
            var parentQn = parent.getQualifiedName();
            if (!parentQn.isEmpty())
                return parentQn.concat("." + name);
        }
        return name;
    }

    @Nullable
    public Package getParent() {
        return parent;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Clazz findClass(SymName name) {
        return classes.get(name);
    }

    public Clazz getClass(String name) {
        return getClass(SymNameTable.instance.get(name));
    }

    public Clazz getClass(SymName name) {
        return Objects.requireNonNull(classes.get(name), () -> "Class " + name + " not found in package " + getQualifiedName());
    }

    public Package findPackage(SymName name) {
        return packages.get(name);
    }

    @Override
    public Collection<Clazz> getClasses() {
        return Collections.unmodifiableCollection(classes.values());
    }

    public Collection<FreeFunc> getFunctions() {
        return Collections.unmodifiableCollection(functions.values());
    }

    public void addClass(Clazz clazz) {
        classes.put(clazz.getName(), clazz);
        table.add(clazz);
    }

    public void addPackage(Package pkg) {
        packages.put(pkg.getName(), pkg);
        table.add(pkg);
    }

    public FreeFunc getFunction(SymName name) {
        return Objects.requireNonNull(functions.get(name), () -> "Function " + name + " not found in package " + name);
    }

    public Package subPackage(String name) {
        var symName = SymNameTable.instance.get(name);
        return subPackage(symName);
    }

    public Package subPackage(SymName name) {
        var pkg = packages.get(name);
        if (pkg != null)
            return pkg;
        return new Package(name, this, project);
    }

    public  Package getPackage(SymName name) {
        return Objects.requireNonNull(packages.get(name),
                () -> "Package '" + getPackageName(getQualifiedName(), name)  + "' not found");
    }

    public void addFunction(FreeFunc function) {
        function.initInstance();
        functions.put(function.getName(), function);
        table.add(function.getInstance());
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitPackage(this);
    }

    @Override
    public void forEachChild(Consumer<Element> action) {
        classes.values().forEach(action);
        functions.values().forEach(action);
        packages.values().forEach(action);
    }

    public void forEachPackage(Consumer<? super Package> action) {
        action.accept(this);
        packages.values().forEach(p -> p.forEachPackage(action));
    }

    @Override
    public void write(ElementWriter writer) {
        throw new UnsupportedOperationException();
    }

    public ElementTable getTable() {
        return table;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public Package getRoot() {
        if (parent == null)
            return this;
        return parent.getRoot();
    }

    @Override
    public String toString() {
        return "package " + (name.isEmpty() ? "<default>" : name);
    }

    public static SymName getPackageName(SymName parentName, SymName name) {
        return parentName.isEmpty() ? name : parentName.concat("." + name);
    }

}
