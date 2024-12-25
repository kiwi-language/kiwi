package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.StdKlass;
import org.metavm.object.type.ClassType;

import javax.annotation.Nullable;

public class LoadAncestorTypeNode extends Node {

    private final ClassType type;

    public LoadAncestorTypeNode(@NotNull String name, @Nullable Node previous, @NotNull Code code, ClassType type) {
        super(name, StdKlass.classType.type(), previous, code);
        this.type = type;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLoadAncestorTypeNode(this);
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("lt_ancestor " + type.getTypeDesc());
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.LT_ANCESTOR);
        output.writeConstant(type);
    }

    @Override
    public int getLength() {
        return 3;
    }

}
