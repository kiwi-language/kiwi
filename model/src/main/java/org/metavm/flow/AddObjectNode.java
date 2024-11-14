package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType
public class AddObjectNode extends Node {

    private boolean ephemeral;

    public AddObjectNode(String name, boolean ephemeral, ClassType type, Node prev,
                         Code code) {
        super(name, type, prev, code);
        this.ephemeral = ephemeral;
    }

    @Override
    @NotNull
    public ClassType getType() {
        return (ClassType) NncUtils.requireNonNull(super.getType());
    }

    @Override
    protected void setOutputType(@Nullable Type outputType) {
        throw new UnsupportedOperationException();
    }

    public Klass getKlass() {
        return getType().resolve();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("allocate " + getType().getName());
    }

    @Override
    public int getStackChange() {
        return 1 - getKlass().getAllFields().size();
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

    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
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

}
