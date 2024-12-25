package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.StdKlass;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;

public class LoadTypeNode extends Node {

    private final Type type;

    public LoadTypeNode(@NotNull String name, @Nullable Node previous, @NotNull Code code, Type type) {
        super(name, null, previous, code);
        this.type = type;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLoadTypeNode(this);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("loadType " + type.toExpression());
    }

    @Override
    public int getStackChange() {
        return 1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.LOAD_KLASS);
        output.writeConstant(type);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @NotNull
    @Override
    public Type getType() {
        return StdKlass.type.type();
    }

    @Override
    public boolean hasOutput() {
        return true;
    }
}
