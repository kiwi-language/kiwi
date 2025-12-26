package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Entity
public class LoadParentNode extends Node {

    public final int index;

    public LoadParentNode(@NotNull String name, @Nullable Node previous, @NotNull Code code, int index) {
        super(name, Types.getAnyType(), previous, code);
        this.index = index;
    }

    public static Node read(CodeInput input, String name) {
        return new LoadParentNode(name, input.getPrev(), input.getCode(), input.readShort());
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("loadParent " + index);
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.LOAD_PARENT);
        output.writeShort(index);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLoadParentNode(this);
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
