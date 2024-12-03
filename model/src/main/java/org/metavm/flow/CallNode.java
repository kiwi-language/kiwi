package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.ReadWriteArray;
import org.metavm.object.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@EntityType
public abstract class CallNode extends Node {

    public static final Logger logger = LoggerFactory.getLogger(CallNode.class);

    private FlowRef flowRef;
    @ChildEntity
    protected final ReadWriteArray<Type> capturedVariableTypes = addChild(new ReadWriteArray<>(Type.class), "capturedVariableTypes");
    @ChildEntity
    protected final ReadWriteArray<Integer> capturedVariableIndexes = addChild(new ReadWriteArray<>(Integer.class), "capturedVariableIndexes");

    public CallNode(String name, Node prev, Code code, @NotNull FlowRef flowRef) {
        super(name, null, prev, code);
        this.flowRef = flowRef;
    }

    public FlowRef getFlowRef() {
        return flowRef;
    }

    public void setFlowRef(FlowRef flowRef) {
        this.flowRef = flowRef;
    }

    public void setCapturedVariableTypes(List<Type> capturedVariableTypes) {
        this.capturedVariableTypes.reset(capturedVariableTypes);
    }

    public void setCapturedVariableIndexes(List<Integer> capturedVariableIndexes) {
        this.capturedVariableIndexes.reset(capturedVariableIndexes);
    }

    public int getCapturedVariableCount() {
        return capturedVariableIndexes.size();
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
    public void writeContent(CodeWriter writer) {
        writer.write("invoke " + flowRef);
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
        output.writeShort(capturedVariableTypes.size());
        for (var capturedVariableIndex : capturedVariableIndexes) {
            output.writeShort(capturedVariableIndex.shortValue());
        }
        for (Type capturedVariableType : capturedVariableTypes) {
            output.writeConstant(capturedVariableType);
        }
    }

    @Override
    public int getLength() {
        return 5 + (capturedVariableIndexes.size() << 2);
    }
}
