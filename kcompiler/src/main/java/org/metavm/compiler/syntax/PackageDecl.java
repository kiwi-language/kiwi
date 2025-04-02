package org.metavm.compiler.syntax;

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
}
