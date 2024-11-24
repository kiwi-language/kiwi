package org.metavm.flow;

import org.metavm.entity.ElementVisitor;

public class VoidReturnNode extends Node {

    public VoidReturnNode(String name, Node prev, Code code) {
        super(name, null, prev, code);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("void-ret");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.VOID_RETURN);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public boolean isExit() {
        return true;
    }

    @Override
    public boolean hasOutput() {
        return false;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitReturn(this);
    }

}
