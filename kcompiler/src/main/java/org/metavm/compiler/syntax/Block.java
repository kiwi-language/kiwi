package org.metavm.compiler.syntax;

import org.metavm.compiler.util.List;

import java.util.Objects;
import java.util.function.Consumer;

public final class Block extends Node {
    private List<Stmt> stmts;

    public Block(List<Stmt> stmts) {
        this.stmts = stmts;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.writeln("{");
        writer.indent();
        for (Stmt stmt : stmts) {
            writer.write(stmt);
            writer.writeln();
        }
        writer.deIndent();
        writer.write("}");
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitBlock(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        stmts.forEach(action);
    }

    public List<Stmt> getStmts() {
        return stmts;
    }

    public void setStmts(List<Stmt> stmts) {
        this.stmts = stmts;
    }

    void addStmt(Stmt stmt) {
        this.stmts.add(stmt);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Block) obj;
        return Objects.equals(this.stmts, that.stmts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stmts);
    }

}
