package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.ClassType;
import org.metavm.util.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@EntityType
public class NewObjectNode extends CallNode {

    public static final Logger logger = LoggerFactory.getLogger(NewObjectNode.class);

    private boolean ephemeral;

    private boolean unbound;

    public NewObjectNode(String name, MethodRef methodRef,
                         Node prev, Code code,
                         boolean ephemeral, boolean unbound) {
        super(name, prev, code, methodRef);
        this.ephemeral = ephemeral;
        this.unbound = unbound;
    }

    @Override
    public MethodRef getFlowRef() {
        return (MethodRef) super.getFlowRef();
    }

    @Override
    public void setFlowRef(FlowRef flowRef) {
        if (flowRef instanceof MethodRef)
            super.setFlowRef(flowRef);
        else
            throw new InternalException("Invalid subflow for NewObjectNode: " + flowRef);
    }

    @Override
    @NotNull
    public ClassType getType() {
        return (ClassType) Objects.requireNonNull(super.getType());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        super.writeContent(writer);
        if (ephemeral)
            writer.write(" ephemeral");
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.NEW);
        writeCallCode(output);
        output.writeBoolean(ephemeral);
        output.writeBoolean(unbound);
    }

    @Override
    public int getLength() {
        return super.getLength() + 2;
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
