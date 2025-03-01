package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Element;
import org.metavm.compiler.element.ValueElement;
import org.metavm.compiler.type.FunctionType;
import org.metavm.compiler.util.List;

import java.util.function.Consumer;

public class CallExpr extends Expr {

    private Expr func;
    private List<TypeNode> typeArguments;
    private List<Expr> arguments;

    public CallExpr(Expr func, List<TypeNode> typeArguments, List<Expr> arguments) {
        this.func = func;
        this.typeArguments = typeArguments;
        this.arguments = arguments;
    }

    public Expr getFunc() {
        return func;
    }

    public void setFunc(Expr func) {
        this.func = func;
    }

    public List<TypeNode> getTypeArguments() {
        return typeArguments;
    }

    public void setTypeArguments(List<TypeNode> typeArguments) {
        this.typeArguments = typeArguments;
    }

    public List<Expr> getArguments() {
        return arguments;
    }

    public void setArguments(List<Expr> arguments) {
        this.arguments = arguments;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(func);
        if (typeArguments.nonEmpty()) {
            writer.write("<");
            writer.write(typeArguments);
            writer.write(">");
        }
        writer.write("(");
        writer.write(arguments);
        writer.write(")");
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitCallExpr(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(func);
        typeArguments.forEach(action);
        arguments.forEach(action);
    }

    @Override
    public void setElement(Element element) {
        if (element instanceof ValueElement e && e.getType() instanceof FunctionType funcType) {
            super.setElement(element);
            setType(funcType.getReturnType());
        }
        else
            throw new IllegalArgumentException("Invalid call expr element: " + element);
    }
}
