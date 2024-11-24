package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

@EntityType
public class CopyNode extends Node {

    protected CopyNode(String name, Node previous, Code code) {
        super(name, null, previous, code);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitCopyNode(this);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("copy");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.COPY);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    @NotNull
    public Type getType() {
        return Types.getAnyType();
    }

    @Override
    public boolean hasOutput() {
        return true;
    }
}
