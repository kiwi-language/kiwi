package org.metavm.flow;

import org.metavm.entity.ElementVisitor;

public class NewChildNode extends NewObjectNode {

    public NewChildNode(String name, MethodRef methodRef, Node prev, Code code) {
        super(name, methodRef, prev, code, false, false);
    }

    @Override
    public int getStackChange() {
        return super.getStackChange() - 1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.NEW_CHILD);
        writeCallCode(output);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("newChild " + getFlowRef());
    }

    @Override
    public int getLength() {
        return 5 + (capturedVariableIndexes.size() << 2);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNewChild(this);
    }

}
