package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;

@Entity
public class GenericInvokeFunctionNode extends GenericInvokeNode {

    public GenericInvokeFunctionNode(String name,
                                     Node prev,
                                     Code code,
                                     FunctionRef function) {
        super(name, prev, code, function);
    }

    @Override
    public FunctionRef getFlowRef() {
        return (FunctionRef) super.getFlowRef();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("ginvokefunction " + getFlowRef());
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.GENERIC_INVOKE_FUNCTION);
        writeCallCode(output);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGenericInvokeFunctionNode(this);
    }
}
