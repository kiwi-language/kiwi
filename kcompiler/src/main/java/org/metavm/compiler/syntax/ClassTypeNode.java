package org.metavm.compiler.syntax;

import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.type.Types;

import java.util.Objects;
import java.util.function.Consumer;

public final class ClassTypeNode extends TypeNode {

    private Expr expr;

    public ClassTypeNode(Expr expr) {
        this.expr = expr;
    }

    public Node getExpr() {
        return expr;
    }

    public void setExpr(Expr expr) {
        this.expr = expr;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(expr);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitClassTypeNode(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(expr);
    }

    @Override
    protected Type actualResolve(Env env) {
        return Types.resolveType(expr, env);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ClassTypeNode that = (ClassTypeNode) object;
        return Objects.equals(expr, that.expr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expr);
    }
}