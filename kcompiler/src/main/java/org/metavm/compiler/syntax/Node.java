package org.metavm.compiler.syntax;

import java.util.function.Consumer;

public abstract class Node {

    public String getText() {
        var w = new SyntaxWriter();
        write(w);
        return w.toString();
    }

    public abstract void write(SyntaxWriter writer);

    public abstract <R> R accept(NodeVisitor<R> visitor);

    public abstract void forEachChild(Consumer<Node> action);

}
