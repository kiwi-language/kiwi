package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;

@EntityType
public class RaiseNode extends Node {

    public RaiseNode(String name, Node prev, Code code) {
        super(name, null, prev, code);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("raise");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.RAISE);
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
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitRaiseNode(this);
    }
}
