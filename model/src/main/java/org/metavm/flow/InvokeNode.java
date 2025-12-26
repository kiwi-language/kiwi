package org.metavm.flow;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Type;

import java.util.function.Consumer;

@Getter
@Entity
public abstract class InvokeNode extends Node {

    private final FlowRef flowRef;

    public InvokeNode(String name, Node prev, Code code, @NotNull FlowRef flowRef) {
        super(name, null, prev, code);
        this.flowRef = flowRef;
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

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        flowRef.accept(visitor);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        flowRef.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }
}
