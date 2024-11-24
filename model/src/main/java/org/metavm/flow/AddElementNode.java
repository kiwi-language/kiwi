package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;

@EntityType
public class AddElementNode extends Node {

    public AddElementNode(String name, Node previous, Code code) {
        super(name, null, previous, code);
    }

    @Override
    public boolean hasOutput() {
        return false;
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
