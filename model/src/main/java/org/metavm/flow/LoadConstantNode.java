package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;

public class LoadConstantNode extends Node {

    private final Value value;

    public LoadConstantNode(Long tmpId, @NotNull String name, @Nullable Node previous, @NotNull Code code, Value value) {
        super(tmpId, name, null, previous, code);
        this.value = value;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLoadConstantNode(this);
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
        return value.getType();
    }
}
