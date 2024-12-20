package org.metavm.flow;

import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.ClassType;

public class NewChildNode extends NewObjectNode {

    public NewChildNode(String name, ClassType type, Node prev, Code code) {
        super(name, type, prev, code, false, false);
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.NEW_CHILD);
        output.writeConstant(getType());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("newchild " + getType());
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNewChild(this);
    }

}
