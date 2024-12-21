package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;

@Entity
public class InvokeStaticNode extends InvokeNode {

    public InvokeStaticNode(String name,
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
        output.write(Bytecodes.INVOKE_STATIC);
        writeCallCode(output);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitInvokeStaticNode(this);
    }
}
