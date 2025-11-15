package org.metavm.flow;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;

import java.util.Objects;
import java.util.function.Consumer;

@Setter
@Entity
public class NewObjectNode extends Node {

    @Getter
    private boolean ephemeral;

    private boolean unbound;

    public NewObjectNode(String name, ClassType type,
                         Node prev, Code code,
                         boolean ephemeral, boolean unbound) {
        super(name, type, prev, code);
        this.ephemeral = ephemeral;
        this.unbound = unbound;
    }

    public static Node read(CodeInput input, String name) {
        return new NewObjectNode(name, (ClassType) input.readConstant(), input.getPrev(), input.getCode(), input.readBoolean(), input.readBoolean());
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
    public void writeContent(CodeWriter writer) {
        writer.write("new " + getType().getTypeDesc());
        if (ephemeral)
            writer.write(" ephemeral");
        if (unbound)
            writer.write(" unbound");
    }

    @Override
    public int getStackChange() {
        return 1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.NEW);
        output.writeConstant(getType());
        output.writeBoolean(ephemeral);
        output.writeBoolean(unbound);
    }

//    public static NewObjectNode read(CodeInput input) {
//        return new NewObjectNode(
//                "NEW",
//                (ClassType) input.readConstant(),
//                input.getPrev(),
//                input.getCode(),
//                input.readBoolean(),
//                input.readBoolean()
//        );
//    }

    @Override
    public int getLength() {
        return 5;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNewObjectNode(this);
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
