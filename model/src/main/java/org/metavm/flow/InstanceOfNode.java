package org.metavm.flow;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Getter
@Entity
public class InstanceOfNode extends Node {

    private final Type targetType;

    public InstanceOfNode(String name,
                          @Nullable Node previous,
                          @NotNull Code code,
                          Type targetType) {
        super(name, Types.getBooleanType(), previous, code);
       this.targetType = targetType;
    }

    public static Node read(CodeInput input, String name) {
        return new InstanceOfNode(name, input.getPrev(), input.getCode(), (Type) input.readConstant());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("instanceof " + targetType.getTypeDesc());
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

    @NotNull
    @Override
    public Type getType() {
        return Types.getBooleanType();
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitInstanceOfNode(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        targetType.accept(visitor);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        targetType.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }
}
