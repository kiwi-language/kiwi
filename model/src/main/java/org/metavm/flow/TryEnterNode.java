package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;

@Entity
public class TryEnterNode extends Node {

    private Node handler;

    public TryEnterNode(String name, Node previous, Code code) {
        super(name, null, previous, code);
    }

    @Override
    public boolean hasOutput() {
        return false;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("tryenter");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.TRY_ENTER);
        output.writeShort(handler.getOffset());
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitTryEnterNode(this);
    }

    public void setHandler(Node handler) {
        this.handler = handler;
    }

}
