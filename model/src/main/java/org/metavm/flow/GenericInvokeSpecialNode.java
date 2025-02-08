package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;

import java.util.Map;
import java.util.function.Consumer;

@Entity
public class GenericInvokeSpecialNode extends GenericInvokeNode {

    @SuppressWarnings("unused")
    private static Klass __klass__;

    public GenericInvokeSpecialNode(String name,
                                    Node prev,
                                    Code code,
                                    MethodRef methodRef) {
        super(name, prev, code, methodRef);
    }

    public static Node read(CodeInput input, String name) {
        return new GenericInvokeSpecialNode(name, input.getPrev(), input.getCode(), (MethodRef) input.readConstant());
    }

    @Override
    public MethodRef getFlowRef() {
        return (MethodRef) super.getFlowRef();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("ginvokespecial " + getFlowRef());
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.GENERIC_INVOKE_SPECIAL);
        writeCallCode(output);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGenericInvokeSpecialNode(this);
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
    public void buildJson(Map<String, Object> map) {
        map.put("flowRef", this.getFlowRef().toJson());
        map.put("stackChange", this.getStackChange());
        map.put("type", this.getType().toJson());
        map.put("length", this.getLength());
        map.put("flow", this.getFlow().getStringId());
        map.put("name", this.getName());
        var successor = this.getSuccessor();
        if (successor != null) map.put("successor", successor.getStringId());
        var predecessor = this.getPredecessor();
        if (predecessor != null) map.put("predecessor", predecessor.getStringId());
        map.put("code", this.getCode().toJson());
        map.put("exit", this.isExit());
        map.put("unconditionalJump", this.isUnconditionalJump());
        map.put("sequential", this.isSequential());
        var error = this.getError();
        if (error != null) map.put("error", error);
        map.put("expressionTypes", this.getExpressionTypes());
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
