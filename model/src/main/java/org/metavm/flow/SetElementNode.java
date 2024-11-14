package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.flow.rest.Bytecodes;

@EntityType
public class SetElementNode extends Node {

    public SetElementNode(Long tmpId, String name, Node previous, Code code) {
        super(tmpId, name, null, previous, code);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("astore");
    }

    @Override
    public int getStackChange() {
        return -3;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.SET_ELEMENT);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitSetElementNode(this);
    }

}
