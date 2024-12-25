package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.StdKlass;

import javax.annotation.Nullable;

public class LoadIntersectionTypeNode extends Node {

    private final int memberCount;

    public LoadIntersectionTypeNode(@NotNull String name, @Nullable Node previous, @NotNull Code code, int memberCount) {
        super(name, StdKlass.intersectionType.type(), previous, code);
        this.memberCount = memberCount;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLoadIntersectionTypeNode(this);
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("ltintersect " + memberCount);
    }

    @Override
    public int getStackChange() {
        return 1 - memberCount;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.LT_INTERSECTION);
        output.writeShort(memberCount);
    }

    @Override
    public int getLength() {
        return 3;
    }
}
