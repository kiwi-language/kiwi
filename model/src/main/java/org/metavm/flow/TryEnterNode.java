package org.metavm.flow;

import lombok.Setter;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;

import java.util.function.Consumer;

@Setter
@Entity
public class TryEnterNode extends Node {

    private Node handler;

    public TryEnterNode(String name, Node previous, Code code) {
        super(name, null, previous, code);
    }


    public TryEnterNode(String name, Node previous, Code code, Node handler) {
        super(name, null, previous, code);
        this.handler = handler;
    }

    public static Node read(CodeInput input, String name) {
        return new TryEnterNode(name, input.getPrev(), input.getCode(), input.readLabel());
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
        output.writeShort(handler.getOffset() - getOffset());
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitTryEnterNode(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        handler.accept(visitor);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }
}
