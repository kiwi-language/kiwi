package org.metavm.flow;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

@Setter
@Entity
public class AddObjectNode extends Node {

    private boolean ephemeral;

    public AddObjectNode(String name, boolean ephemeral, ClassType type, Node prev,
                         Code code) {
        super(name, type, prev, code);
        this.ephemeral = ephemeral;
    }

    public static Node read(CodeInput input, String name) {
        return new AddObjectNode(name, input.readBoolean(), (ClassType) input.readConstant(), input.getPrev(), input.getCode());
    }

    @Override
    @NotNull
    public ClassType getType() {
        return (ClassType) Objects.requireNonNull(super.getType());
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    protected void setOutputType(@Nullable Type outputType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("allocate " + getType().getTypeDesc());
    }

    @Override
    public int getStackChange() {
        return 1 - getType().getKlass().getAllFields().size();
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.ADD_OBJECT);
        output.writeConstant(getType());
        output.writeBoolean(ephemeral);
    }

    @Override
    public int getLength() {
        return 4;
    }

    @Override
    @NotNull
    public Node getSuccessor() {
        return Objects.requireNonNull(super.getSuccessor());
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitAddObjectNode(this);
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
