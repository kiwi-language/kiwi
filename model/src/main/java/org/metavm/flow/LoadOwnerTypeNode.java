package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.StdKlass;

import javax.annotation.Nullable;

public class LoadOwnerTypeNode extends Node {

    public LoadOwnerTypeNode(@NotNull String name, @Nullable Node previous, @NotNull Code code) {
        super(name, StdKlass.genericDeclarationRef.type(), previous, code);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLoadOwnerTypeNode(this);
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("lt_owner");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.LT_OWNER);
    }

    @Override
    public int getLength() {
        return 1;
    }

}
