package org.metavm.compiler.syntax;

import java.util.function.Consumer;

public class BlockStmt extends Stmt {

    private Block block;

    public BlockStmt(Block block) {
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(block);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitBlockStmt(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(block);
    }
}
