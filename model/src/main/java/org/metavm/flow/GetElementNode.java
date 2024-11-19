package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

@EntityType
public class GetElementNode extends Node {

    public GetElementNode(String name, Node previous, Code code) {
        super(name, null, previous, code);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("array-load");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.GET_ELEMENT);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGetElementNode(this);
    }

    @NotNull
    @Override
    public Type getType() {
        return Types.getAnyType();
    }
}
