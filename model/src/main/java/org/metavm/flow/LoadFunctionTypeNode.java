package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.StdKlass;

import javax.annotation.Nullable;

public class LoadFunctionTypeNode extends Node {

    private final int parameterCount;

    public LoadFunctionTypeNode(@NotNull String name, @Nullable Node previous, @NotNull Code code, int parameterCount) {
        super(name, StdKlass.functionType.type(), previous, code);
        this.parameterCount = parameterCount;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLoadFunctionTypeNode(this);
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("ltfunctype");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.LT_FUNCTION_TYPE);
        output.writeShort(parameterCount);
    }

    @Override
    public int getLength() {
        return 3;
    }
}
