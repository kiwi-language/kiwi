package org.metavm.compiler.syntax;

import java.util.Objects;
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

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        BlockStmt blockStmt = (BlockStmt) object;
        return Objects.equals(block, blockStmt.block);
    }

    @Override
    public int hashCode() {
        return Objects.hash(block);
    }
}
