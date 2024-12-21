package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.util.InternalException;

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
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.INVOKE_FUNCTION);
        writeCallCode(output);
    }

    @Override
    public FunctionRef getFlowRef() {
        return (FunctionRef) super.getFlowRef();
    }

    @Override
    public void setFlowRef(FlowRef flowRef) {
        if(flowRef instanceof FunctionRef)
            super.setFlowRef(flowRef);
        else
            throw new InternalException("Invalid sub flow for function call node: " + flowRef);
    }
}
