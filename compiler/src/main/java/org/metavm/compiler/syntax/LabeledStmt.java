package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Name;

import java.util.Objects;
import java.util.function.Consumer;

public class LabeledStmt extends Stmt {

    private Name label;
    private Stmt stmt;

    public LabeledStmt(Name label, Stmt stmt) {
        this.label = label;
        this.stmt = stmt;
    }

    public Name getLabel() {
        return label;
    }

    public void setLabel(Name label) {
        this.label = label;
    }

    public Stmt getStmt() {
        return stmt;
    }

    public void setStmt(Stmt stmt) {
        this.stmt = stmt;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(label);
        writer.write(": ");
        writer.write(stmt);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitLabeledStmt(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(stmt);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        LabeledStmt that = (LabeledStmt) object;
        return Objects.equals(label, that.label) && Objects.equals(stmt, that.stmt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, stmt);
    }
}
