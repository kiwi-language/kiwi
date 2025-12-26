package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ArrayType;

import java.util.Objects;
import java.util.function.Consumer;

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

    public static Node read(CodeInput input, String name) {
        return new NewArrayWithDimsNode(name, (ArrayType) input.readConstant(), input.getPrev(), input.getCode(), input.readShort());
    }

    @Override
    @NotNull
    public ArrayType getType() {
        return (ArrayType) Objects.requireNonNull(super.getType());
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
