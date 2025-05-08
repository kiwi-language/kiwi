package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Name;

import java.util.function.Consumer;

public class Init extends Node {

    private Block body;

    public Init(Block body) {
        this.body = body;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(Name.init());
        writer.write(" ");
        writer.write(body);
        writer.writeln();
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitInit(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(body);
    }
}
