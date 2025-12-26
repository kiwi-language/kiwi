package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;

import java.util.function.Consumer;

@Entity
public class VoidReturnNode extends Node {

    public VoidReturnNode(String name, Node prev, Code code) {
        super(name, null, prev, code);
    }

    public static Node read(CodeInput input, String name) {
        return new VoidReturnNode(name, input.getPrev(), input.getCode());
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
        return visitor.visitVoidReturnNode(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
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
