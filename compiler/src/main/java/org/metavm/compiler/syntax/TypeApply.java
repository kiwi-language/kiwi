package org.metavm.compiler.syntax;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.element.Element;
import org.metavm.compiler.util.List;

import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
public class TypeApply extends Expr {

    private Expr expr;
    private List<TypeNode> args;

    public TypeApply(Expr expr, List<TypeNode> args) {
        this.expr = expr;
        this.args = args;
    }

    public Expr getExpr() {
        return expr;
    }

    public void setExpr(Expr expr) {
        this.expr = expr;
    }

    public List<TypeNode> getArgs() {
        return args;
    }

    public void setArgs(List<TypeNode> args) {
        this.args = args;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(expr);
        writer.write("<");
        writer.write(args);
        writer.write(">");
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitTypeApply(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(expr);
        args.forEach(action);
    }

    @Override
    public void setElement(Element element) {
        super.setElement(element);
        expr.setElement(element);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        TypeApply typeApply = (TypeApply) object;
        return Objects.equals(expr, typeApply.expr) && Objects.equals(args, typeApply.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expr, args);
    }

}
