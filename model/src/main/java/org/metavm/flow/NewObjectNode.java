package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.ClassType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Entity
public class NewObjectNode extends Node {

    public static final Logger logger = LoggerFactory.getLogger(NewObjectNode.class);

    private boolean ephemeral;

    private boolean unbound;

    public NewObjectNode(String name, ClassType type,
                         Node prev, Code code,
                         boolean ephemeral, boolean unbound) {
        super(name, type, prev, code);
        this.ephemeral = ephemeral;
        this.unbound = unbound;
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

    @Override
    public int getLength() {
        return 5;
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }

    public void setUnbound(boolean unbound) {
        this.unbound = unbound;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNewObjectNode(this);
    }

}
