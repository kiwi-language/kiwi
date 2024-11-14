package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.flow.rest.Bytecodes;

@EntityType
public class AddElementNode extends Node {

    public AddElementNode(Long tmpId, String name, Node previous, Code code) {
        super(tmpId, name, null, previous, code);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("arrayadd");
    }

    @Override
    public int getStackChange() {
        return -2;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.ADD_ELEMENT);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitAddElementNode(this);
    }

}
