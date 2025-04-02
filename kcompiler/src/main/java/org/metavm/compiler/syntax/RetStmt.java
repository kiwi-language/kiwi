package org.metavm.compiler.syntax;

import org.metavm.compiler.type.Type;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public final class RetStmt extends Stmt {
    @Nullable
    private final Expr result;
    private Type type;

    public RetStmt(@Nullable Expr result) {
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        assert type.isVoid() == (result == null) : getText() + " is assigned a void type";
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RetStmt) obj;
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
