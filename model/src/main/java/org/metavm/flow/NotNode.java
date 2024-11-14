package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class NotNode extends Node {

    public NotNode(Long tmpId,
                   @NotNull String name,
                   @Nullable Node previous,
                   @NotNull Code code) {
        super(tmpId, name, null, previous, code);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNodeNode(this);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("not");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.NOT);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @NotNull
    @Override
    public Type getType() {
        return Types.getBooleanType();
    }

}
