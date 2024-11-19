package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;

@EntityType
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
    public void writeContent(CodeWriter writer) {
        var method = getFlowRef().resolve();
        writer.write("invoke " + method.getQualifiedSignature());
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.METHOD_CALL);
        writeCallCode(output);
    }

    private Method getMethod() {
        return (Method) super.getFlowRef().resolve();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitSubFlowNode(this);
    }
}
