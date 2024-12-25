package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.StdKlass;

import javax.annotation.Nullable;

public class LoadUnionTypeNode extends Node {

    private final int memberCount;

    public LoadUnionTypeNode(@NotNull String name, @Nullable Node previous, @NotNull Code code, int memberCount) {
        super(name, StdKlass.unionType.type(), previous, code);
        this.memberCount = memberCount;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLoadUnionTypeNode(this);
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("ltunion " + memberCount);
    }

    @Override
    public int getStackChange() {
        return 1 - memberCount;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.LT_UNION);
        output.writeShort(memberCount);
    }

    @Override
    public int getLength() {
        return 3;
    }
}
