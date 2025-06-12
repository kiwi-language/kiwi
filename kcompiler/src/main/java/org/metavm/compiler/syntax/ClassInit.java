package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Method;

import java.util.Objects;
import java.util.function.Consumer;

public final class ClassInit extends Decl<Method> {
    private final Block block;

    public ClassInit(Block block) {
        this.block = block;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write("static ");
        writer.write(block);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitClassInit(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(block);
    }

    public Block block() {
        return block;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ClassInit) obj;
        return Objects.equals(this.block, that.block);
    }

    @Override
    public int hashCode() {
        return Objects.hash(block);
    }

}
