package org.metavm.compiler.syntax;

import java.util.Objects;
import java.util.function.Consumer;

public class PackageDecl extends Node {

    private Expr name;

    public PackageDecl(Expr name) {
        this.name = name;
    }

    public Expr getName() {
        return name;
    }

    public void setName(Expr name) {
        this.name = name;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write("package ");
        writer.write(name);
        writer.writeln();
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitPackageDecl(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(name);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        PackageDecl that = (PackageDecl) object;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
