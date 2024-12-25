package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;

@Entity
public class GenericInvokeVirtualNode extends GenericInvokeNode {

    public GenericInvokeVirtualNode(String name,
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
        writer.write("ginvokevirtual " + getFlowRef());
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.GENERIC_INVOKE_VIRTUAL);
        writeCallCode(output);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGenericInvokeVirtualNode(this);
    }
}
