package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Consumer;

@Entity
public abstract class InvokeNode extends Node {

    public static final Logger logger = LoggerFactory.getLogger(InvokeNode.class);
    @SuppressWarnings("unused")
    private static Klass __klass__;

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
    public void buildJson(Map<String, Object> map) {
        map.put("flowRef", this.getFlowRef().toJson());
        map.put("type", this.getType().toJson());
        map.put("stackChange", this.getStackChange());
        map.put("length", this.getLength());
        map.put("flow", this.getFlow().getStringId());
        map.put("name", this.getName());
        var successor = this.getSuccessor();
        if (successor != null) map.put("successor", successor.getStringId());
        var predecessor = this.getPredecessor();
        if (predecessor != null) map.put("predecessor", predecessor.getStringId());
        map.put("code", this.getCode().getStringId());
        map.put("exit", this.isExit());
        map.put("unconditionalJump", this.isUnconditionalJump());
        map.put("sequential", this.isSequential());
        var error = this.getError();
        if (error != null) map.put("error", error);
        map.put("expressionTypes", this.getExpressionTypes());
        map.put("text", this.getText());
        map.put("nextExpressionTypes", this.getNextExpressionTypes());
        map.put("offset", this.getOffset());
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }
}
