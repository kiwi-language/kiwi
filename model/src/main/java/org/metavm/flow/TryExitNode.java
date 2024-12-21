package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;

@Entity
public class TryExitNode extends Node {

    public TryExitNode(String name, Node previous, Code code) {
        super(name, null, previous, code);
    }

    @Override
    public boolean hasOutput() {
        return false;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("tryexit");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.TRY_EXIT);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitTryExitNode(this);
    }

}
