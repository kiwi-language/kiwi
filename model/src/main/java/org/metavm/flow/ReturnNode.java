package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Type;

import java.util.function.Consumer;

@Entity
@Slf4j
public class ReturnNode extends Node {

    public ReturnNode(String name, Node prev, Code code) {
        super(name, null, prev, code);
    }

    public static Node read(CodeInput input, String name) {
        return new ReturnNode(name, input.getPrev(), input.getCode());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("return");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.RETURN);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    @NotNull
    public Type getType() {
        return getCode().getCallable().getReturnType();
    }

    @Override
    public boolean hasOutput() {
        return false;
    }

    @Override
    public boolean isExit() {
        return true;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitReturnNode(this);
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
