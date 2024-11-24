package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.ArrayType;
import org.metavm.util.NncUtils;

@EntityType
public class NewArrayNode extends Node {

    public NewArrayNode(String name,
                        ArrayType type,
                        Node previous,
                        Code code) {
        super(name, type, previous, code);
    }

    @Override
    @NotNull
    public ArrayType getType() {
        return (ArrayType) NncUtils.requireNonNull(super.getType());
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("new " + getType().getName());
    }

    @Override
    public int getStackChange() {
        return 1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.NEW_ARRAY);
        output.writeConstant(getType());
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNewArrayNode(this);
    }
}
