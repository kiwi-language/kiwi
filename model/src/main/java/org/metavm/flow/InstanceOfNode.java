package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class InstanceOfNode extends Node {

    private final Type targetType;

    public InstanceOfNode(String name,
                          @Nullable Node previous,
                          @NotNull Code code,
                          Type targetType) {
        super(name, Types.getBooleanType(), previous, code);
       this.targetType = targetType;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitInstanceOfNode(this);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("instanceof");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.INSTANCE_OF);
        output.writeConstant(targetType);
    }

    @Override
    public int getLength() {
        return 3;
    }

    public Type getTargetType() {
        return targetType;
    }

    @NotNull
    @Override
    public Type getType() {
        return Types.getBooleanType();
    }

}
