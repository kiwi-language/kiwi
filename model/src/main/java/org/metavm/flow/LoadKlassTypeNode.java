package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.StdKlass;
import org.metavm.object.type.KlassType;

import javax.annotation.Nullable;

public class LoadKlassTypeNode extends Node {

    private final KlassType type;

    public LoadKlassTypeNode(@NotNull String name, @Nullable Node previous, @NotNull Code code, KlassType type) {
        super(name, StdKlass.klassType.type(), previous, code);
        this.type = type;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLoadKlassTypeNode(this);
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("ltkt " + type.getTypeDesc());
    }

    @Override
    public int getStackChange() {
        return 1 - type.klass.getTypeParameters().size();
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.LT_KLASS);
        output.writeConstant(type);
    }

    @Override
    public int getLength() {
        return 3;
    }
}
