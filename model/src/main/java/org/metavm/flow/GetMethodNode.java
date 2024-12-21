package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;

public class GetMethodNode extends Node {

    private final MethodRef methodRef;

    public GetMethodNode(String name,
                         @Nullable Node previous,
                         @NotNull Code code,
                         MethodRef methodRef) {
        super(name, null, previous, code);
        this.methodRef = methodRef;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGetMethod(this);
    }

    @NotNull
    public Type getType() {
        return methodRef.getType();
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("getmethod " + methodRef);
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.GET_METHOD);
        output.writeConstant(methodRef);
    }

    @Override
    public int getLength() {
        return 3;
    }
}
