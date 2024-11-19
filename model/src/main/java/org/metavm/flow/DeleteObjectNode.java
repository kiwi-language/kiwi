package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;

@EntityType
public class DeleteObjectNode extends Node {

    public DeleteObjectNode(String name, Node prev, Code code) {
        super(name, null, prev, code);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("delete");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.DELETE_OBJECT);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitDeleteObjectNode(this);
    }
}
