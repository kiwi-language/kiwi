package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.ArrayType;
import org.metavm.util.NncUtils;

@Entity
public class NewArrayWithDimsNode extends Node {

    private final int dimensions;

    public NewArrayWithDimsNode(String name,
                                ArrayType type,
                                Node previous,
                                Code code,
                                int dimensions) {
        super(name, type, previous, code);
        this.dimensions = dimensions;
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
        writer.write("new " + getType().getName() + " dimensions = " + dimensions);
    }

    @Override
    public int getStackChange() {
        return 1 - dimensions;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.NEW_ARRAY_WITH_DIMS);
        output.writeConstant(getType());
        output.writeShort(dimensions);
    }

    @Override
    public int getLength() {
        return 5;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNewArrayWithDimsNode(this);
    }
}
