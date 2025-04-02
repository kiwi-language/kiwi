package org.metavm.compiler.syntax;

import org.metavm.compiler.util.List;

import java.util.Objects;
import java.util.function.Consumer;

public class AnonClassExpr extends Expr {
    private List<Expr> arguments;
    private ClassDecl decl;

    public AnonClassExpr(List<Expr> arguments, ClassDecl decl) {
        this.arguments = arguments;
        this.decl = decl;
    }

    public List<Expr> getArguments() {
        return arguments;
    }

    public void setArguments(List<Expr> arguments) {
        this.arguments = arguments;
    }

    public ClassDecl getDecl() {
        return decl;
    }

    public void setDecl(ClassDecl decl) {
        this.decl = decl;
    }

    @Override
    public void write(SyntaxWriter writer) {
        var decl = Objects.requireNonNull(this.decl);
        decl.getImplements().getFirst().write(writer);
        writer.write("(");
        writer.write(arguments);
        writer.write(")");
        decl.writeBody(writer);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitAnonClassExpr(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        arguments.forEach(action);
        action.accept(decl);
    }
}
