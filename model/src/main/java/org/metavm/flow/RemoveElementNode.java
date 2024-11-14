package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.object.type.Types;

@EntityType
public class RemoveElementNode extends Node {

    public RemoveElementNode(Long tmpId, String name, Node previous, Code code) {
        super(tmpId, name, Types.getBooleanType(), previous, code);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("removeElement");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.DELETE_ELEMENT);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitDeleteElementNode(this);
    }
}
