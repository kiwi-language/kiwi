package org.metavm.compiler.syntax;

import java.util.function.Consumer;

public class Catcher extends Node {

    private LocalVarDecl param;
    private Block block;

    public Catcher(LocalVarDecl param, Block block) {
        this.param = param;
        this.block = block;
    }

    public LocalVarDecl getParam() {
        return param;
    }

    public void setParam(LocalVarDecl param) {
        this.param = param;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write("catch (");
        writer.write(param);
        writer.write(") ");
        writer.write(block);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitCatcher(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(param);
        action.accept(block);
    }
}
