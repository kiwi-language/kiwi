package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;

import java.util.function.Consumer;

@Entity
public class InvokeFunctionNode extends InvokeNode {

    public InvokeFunctionNode(String name, Node prev, Code code, FunctionRef functionRef) {
        super(name,  prev, code, functionRef);
    }

    public static Node read(CodeInput input, String name) {
        return new InvokeFunctionNode(name, input.getPrev(), input.getCode(), (FunctionRef) input.readConstant());
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

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitInvokeFunctionNode(this);
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
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }
}
