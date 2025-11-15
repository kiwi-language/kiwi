package org.metavm.flow;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Getter
@Entity
public class LoadConstantNode extends Node {

    private final Value value;

    public LoadConstantNode(@NotNull String name, @Nullable Node previous, @NotNull Code code, Value value) {
        super(name, null, previous, code);
        this.value = value;
    }

    public static Node read(CodeInput input, String name) {
        return new LoadConstantNode(name, input.getPrev(), input.getCode(), input.readConstant());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("ldc " + value.getText());
    }

    @Override
    public int getStackChange() {
        return 1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.LOAD_CONSTANT);
        output.writeConstant(value);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    @NotNull
    public Type getType() {
        return value.getValueType();
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLoadConstantNode(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        if (value instanceof Reference r) action.accept(r);
        else if (value instanceof org.metavm.object.instance.core.NativeValue t) t.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

}
