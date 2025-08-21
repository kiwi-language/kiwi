package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Name;
import org.metavm.compiler.element.Variable;
import org.metavm.compiler.util.CompilationException;

import java.util.Objects;
import java.util.function.Consumer;

public final class SelectorExpr extends Expr {
    private final Expr x;
    private final Name sel;

    public SelectorExpr(Expr x, Name sel) {
        this.x = x;
        this.sel = sel;
    }

    @Override
    public void write(SyntaxWriter writer) {
        x.write(writer);
        writer.write(".");
        writer.write(sel);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitSelectorExpr(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(x);
    }

    public Expr x() {
        return x;
    }

    public Name sel() {
        return sel;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        SelectorExpr that = (SelectorExpr) object;
        return Objects.equals(x, that.x) && Objects.equals(sel, that.sel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, sel);
    }

    @Override
    public boolean isMutable() {
        var e = getElement();
        if (e == null)
            throw new CompilationException("Expression not yet attributed: " + getText());
        return e instanceof Variable v && v.isMutable();
    }
}
