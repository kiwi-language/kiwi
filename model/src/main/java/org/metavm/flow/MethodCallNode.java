package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;

@Entity
public class MethodCallNode extends CallNode {

    public MethodCallNode(String name,
                          Node prev,
                          Code code,
                          MethodRef methodRef) {
        super(name, prev, code, methodRef);
    }

    @Override
    public MethodRef getFlowRef() {
        return (MethodRef) super.getFlowRef();
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.METHOD_CALL);
        writeCallCode(output);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitSubFlowNode(this);
    }
}
