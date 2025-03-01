package org.metavm.compiler.syntax;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public final class ReturnStmt extends Stmt {
    @Nullable
    private final Expr result;

    public ReturnStmt(@Nullable Expr result) {
        this.result = result;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write("return");
        if (result != null) {
            writer.write(" ");
            result.write(writer);
        }
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitReturnStmt(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        if (result != null)
            action.accept(result);
    }

    @Nullable
    public Expr result() {
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ReturnStmt) obj;
        return Objects.equals(this.result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result);
    }

    @Override
    public String toString() {
        return "ReturnStmt[" +
                "result=" + result + ']';
    }

}
