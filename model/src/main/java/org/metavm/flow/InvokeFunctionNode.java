package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;

@Entity
public class InvokeFunctionNode extends InvokeNode {

    public InvokeFunctionNode(String name, Node prev, Code code, FunctionRef functionRef) {
        super(name,  prev, code, functionRef);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitInvokeFunction(this);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("invokefunction " + getFlowRef());
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.INVOKE_FUNCTION);
        writeCallCode(output);
    }

    @Override
    public FunctionRef getFlowRef() {
        return (FunctionRef) super.getFlowRef();
    }

}
