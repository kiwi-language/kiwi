package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.object.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public abstract class InvokeNode extends Node {

    public static final Logger logger = LoggerFactory.getLogger(InvokeNode.class);

    private final FlowRef flowRef;

    public InvokeNode(String name, Node prev, Code code, @NotNull FlowRef flowRef) {
        super(name, null, prev, code);
        this.flowRef = flowRef;
    }

    public FlowRef getFlowRef() {
        return flowRef;
    }

    @Override
    public Type getType() {
        var type = getFlowRef().getReturnType();
        return type.isVoid() ? null : type;
    }

    @Override
    public boolean hasOutput() {
        return !getFlowRef().getRawFlow().getReturnType().isVoid();
    }

    @Override
    public int getStackChange() {
        var flow = flowRef.getRawFlow();
        if(flow.getReturnType().isVoid())
            return -flow.getInputCount();
        else
            return 1 - flow.getInputCount();
    }

    public void writeCallCode(CodeOutput output) {
        output.writeConstant(flowRef);
    }

    @Override
    public int getLength() {
        return 3;
    }
}
