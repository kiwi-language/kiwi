package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Package;
import org.metavm.compiler.util.List;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public final class File extends Node {
    private final @Nullable PackageDecl packageDecl;
    private final List<Import> imports;
    private final List<ClassDecl> classDeclarations;
    private Package package_;


    public File(
            @Nullable PackageDecl packageDecl,
            List<Import> imports,
            List<ClassDecl> classDeclarations
    ) {
        this.packageDecl = packageDecl;
        this.imports = imports;
        this.classDeclarations = classDeclarations;
    }

    @Override
    public void write(SyntaxWriter writer) {
        if (packageDecl != null) {
            writer.write(packageDecl);
            writer.writeln();
        }
        if (!imports.isEmpty()) {
            imports.forEach(writer::write);
            writer.writeln();
        }
        for (ClassDecl classDecl : classDeclarations) {
            classDecl.write(writer);
            writer.writeln();
        }
    }

    @Nullable
    public PackageDecl getPackageDecl() {
        return packageDecl;
    }

    public List<Import> getImports() {
        return imports;
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitFile(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        if (packageDecl != null)
            action.accept(packageDecl);
        classDeclarations.forEach(action);
    }

    public List<ClassDecl> getClassDeclarations() {
        return classDeclarations;
    }

    public Package getPackage() {
        return package_;
    }

    public void setPackage(Package pkg) {
        this.package_ = pkg;
    }

}
