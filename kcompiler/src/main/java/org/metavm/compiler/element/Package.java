package org.metavm.compiler.element;

import org.metavm.compiler.util.List;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class Package extends ElementBase implements ClassScope, Element {
    private Project project;
    private Name name;
    private final @Nullable Package parent;

    private final Map<Name, Clazz> classes = new HashMap<>();
    private final Map<Name, FreeFunc> functions = new HashMap<>();
    private final Map<Name, Package> packages = new HashMap<>();

    private final ElementTable table = new ElementTable();

    public Package(Name name, @Nullable Package parent, Project project) {
        this.name = name;
        this.parent = parent;
        this.project = project;
        if (parent != null)
            parent.addPackage(this);
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public Name getQualName() {
        if (parent != null) {
            var parentQn = parent.getQualName();
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

    public Clazz findClass(Name name) {
        return classes.get(name);
    }

    public Clazz getClass(String name) {
        return getClass(NameTable.instance.get(name));
    }

    public Clazz getClass(Name name) {
        return Objects.requireNonNull(classes.get(name), () -> "Class " + name + " not found in package " + getQualName());
    }

    public Package findPackage(Name name) {
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

    public FreeFunc getFunction(Name name) {
        return Objects.requireNonNull(functions.get(name), () -> "Function " + name + " not found in package " + this.name);
    }

    public Package subPackage(String name) {
        var symName = NameTable.instance.get(name);
        return subPackage(symName);
    }

    public Package subPackage(Name name) {
        var pkg = packages.get(name);
        if (pkg != null)
            return pkg;
        return new Package(name, this, project);
    }

    public  Package getPackage(Name name) {
        return Objects.requireNonNull(packages.get(name),
                () -> "Package '" + getPackageName(getQualName(), name)  + "' not found");
    }

    public Collection<Package> getPackages() {
        return packages.values();
    }

    public void addFunction(FreeFunc function) {
        functions.put(function.getName(), function);
        table.add(function);
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

    public static Name getPackageName(Name parentName, Name name) {
        return parentName.isEmpty() ? name : parentName.concat("." + name);
    }

}
