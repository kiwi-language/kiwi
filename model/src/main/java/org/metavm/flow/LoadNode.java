package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;
import java.util.Objects;

@Slf4j
public class LoadNode extends VariableAccessNode {

    public LoadNode(@NotNull String name, Type outputType,
                    @Nullable Node previous, @NotNull Code code, int index) {
        super(name, outputType, previous, code, index);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLoadNode(this);
    }

    @Override
    @NotNull
    public Type getType() {
        return Objects.requireNonNull(super.getType());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("load " + index);
    }

    @Override
    public int getStackChange() {
        return 1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.LOAD);
        output.writeShort(index);
    }

    @Override
    public int getLength() {
        return 3;
    }

    public int getIndex() {
        return index;
    }
}
