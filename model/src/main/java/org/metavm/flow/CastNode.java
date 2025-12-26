package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Type;

import java.util.Objects;
import java.util.function.Consumer;

@Entity
public class CastNode extends Node {

    public CastNode(String name, @NotNull Type outputType,
                    Node previous, Code code) {
        super(name, outputType, previous, code);
    }

    public static Node read(CodeInput input, String name) {
        return new CastNode(name, (Type) input.readConstant(), input.getPrev(), input.getCode());
    }

    public @NotNull Type getType() {
        return Objects.requireNonNull(super.getType());
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    protected void setOutputType(Type outputType) {
        super.setOutputType(Objects.requireNonNull(outputType));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write( "cast " + getType().getName());
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.CAST);
        output.writeConstant(getType());
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitCastNode(this);
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
