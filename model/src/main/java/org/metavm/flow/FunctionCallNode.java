package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.util.InternalException;

@EntityType
public class FunctionCallNode extends CallNode {

    public FunctionCallNode(Long tmpId, String name, Node prev, Code code, FunctionRef functionRef) {
        super(tmpId, name,  prev, code, functionRef);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFunctionCallNode(this);
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.FUNCTION_CALL);
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
