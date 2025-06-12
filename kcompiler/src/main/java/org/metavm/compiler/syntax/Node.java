package org.metavm.compiler.syntax;

import org.metavm.compiler.diag.DiagPos;

import java.util.function.Consumer;

public abstract class Node implements DiagPos {

    private int pos;

    public Node() {
        this.pos = 0;
    }

    @Override
    public int getIntPos() {
        return pos;
    }

    public Node setPos(int pos) {
        this.pos = pos;
        return this;
    }

    public String getText() {
        var w = new SyntaxWriter();
        write(w);
        return w.toString();
    }

    public abstract void write(SyntaxWriter writer);

    public abstract <R> R accept(NodeVisitor<R> visitor);

    public abstract void forEachChild(Consumer<Node> action);

    @Override
    public String toString() {
        return getText();
    }
}
