package org.metavm.compiler.syntax;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public final class CaseClause extends Node {
    @Nullable
    private final Expr cases;
    private final Stmt body;

    public CaseClause(@Nullable Expr cases, Stmt body) {
        this.cases = cases;
        this.body = body;
    }

    @Override
    public void write(SyntaxWriter writer) {
        if (cases != null) {
            writer.write("case ");
            cases.write(writer);
        } else
            writer.write("default");
        writer.write(" -> ");
        body.write(writer);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitCaseClause(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        if (cases != null)
            action.accept(cases);
        action.accept(body);
    }

    @Nullable
    public Expr cases() {
        return cases;
    }

    public Stmt body() {
        return body;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CaseClause) obj;
        return Objects.equals(this.cases, that.cases) &&
                Objects.equals(this.body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cases, body);
    }

    @Override
    public String toString() {
        return "CaseClause[" +
                "cases=" + cases + ", " +
                "body=" + body + ']';
    }

}
