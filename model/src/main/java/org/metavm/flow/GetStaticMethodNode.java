package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;

public class GetStaticMethodNode extends Node {

    private final MethodRef methodRef;

    public GetStaticMethodNode(String name,
                               @Nullable Node previous,
                               @NotNull Code code,
                               MethodRef methodRef) {
        super(name, null, previous, code);
        this.methodRef = methodRef;
    }

    @NotNull
    @Override
    public Type getType() {
        return methodRef.getPropertyType();
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGetStaticMethodNode(this);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("getstaticmethod " + methodRef);
    }

    @Override
    public int getStackChange() {
        return 1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.GET_STATIC_METHOD);
        output.writeConstant(methodRef);
    }

    @Override
    public int getLength() {
        return 3;
    }

}
