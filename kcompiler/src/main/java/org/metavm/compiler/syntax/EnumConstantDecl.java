package org.metavm.compiler.syntax;

import org.metavm.compiler.element.EnumConstant;
import org.metavm.compiler.util.List;

import java.util.function.Consumer;

public final class EnumConstantDecl extends Decl<EnumConstant> {
    private final Ident name;
    private List<Expr> arguments;

    public EnumConstantDecl(Ident name, List<Expr> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(name);
        writer.writeln(",");
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitEnumConstantDecl(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(name);
    }

    public Ident getName() {
        return name;
    }

    public List<Expr> getArguments() {
        return arguments;
    }

    public void setArguments(List<Expr> arguments) {
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return "EnumConstantDecl[" +
                "name=" + name + ']';
    }

}
