package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Element;
import org.metavm.compiler.element.MethodInst;
import org.metavm.compiler.element.ValueElement;
import org.metavm.compiler.util.List;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public final class NewExpr extends Expr {
    @Nullable
    private final Expr owner;
    private final TypeNode type;
    private final List<Expr> arguments;

    public NewExpr(
            @Nullable Expr owner,
            TypeNode type,
            List<Expr> arguments
    ) {
        this.owner = owner;
        this.type = type;
        this.arguments = arguments;
    }

    @Override
    public void write(SyntaxWriter writer) {
        if (owner != null) {
            writer.write(owner);
            writer.write(".");
        }
        writer.write("new ");
        type.write(writer);
        writer.write("(");
        writer.write(arguments);
        writer.write(")");
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitNewExpr(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        if (owner != null)
            action.accept(owner);
        action.accept(type);
        arguments.forEach(action);
    }

    @Nullable
    public Expr owner() {
        return owner;
    }

    public TypeNode type() {
        return type;
    }

    public List<Expr> arguments() {
        return arguments;
    }

    @Override
    public void setElement(Element element) {
        if (element instanceof MethodInst init) {
            super.setElement(init);
            setType(init.getDeclaringType());
        }
        else
            throw new IllegalArgumentException("Invalid new expr element: " + element);
    }
}
