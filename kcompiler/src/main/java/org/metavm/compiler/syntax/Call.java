package org.metavm.compiler.syntax;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.element.*;
import org.metavm.compiler.type.FuncType;
import org.metavm.compiler.util.List;

import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
public class Call extends Expr {

    private Expr func;
    private List<Expr> arguments;

    public Call(Expr func, List<Expr> arguments) {
        this.func = func;
        this.arguments = arguments;
    }

    public Expr getFunc() {
        return func;
    }

    public void setFunc(Expr func) {
        this.func = func;
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
        writer.write("(");
        writer.write(arguments);
        writer.write(")");
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitCall(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(func);
        arguments.forEach(action);
    }

    @Override
    public void setElement(Element element) {
        if (element instanceof ValueElement e && e.getType() instanceof FuncType funcType) {
            super.setElement(element);
            if (isNewExpr())
                setType(((MethodRef) getElement()).getDeclType());
            else
                setType(funcType.getRetType());
        }
        else
            throw new IllegalArgumentException("Invalid call expr element: " + element);
    }

    public boolean isNewExpr() {
        if (getElement() instanceof MethodRef m && m.isInit())
            return Nodes.getRefName(func) == m.getDeclType().getClazz().getName();
        else
            return false;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Call call = (Call) object;
        return Objects.equals(func, call.func) && Objects.equals(arguments, call.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(func, arguments);
    }

}
