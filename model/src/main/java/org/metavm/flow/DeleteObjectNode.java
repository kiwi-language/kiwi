package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.flow.rest.Bytecodes;

@EntityType
public class DeleteObjectNode extends Node {

    public DeleteObjectNode(Long tmpId, String name, Node prev, Code code) {
        super(tmpId, name, null, prev, code);
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
