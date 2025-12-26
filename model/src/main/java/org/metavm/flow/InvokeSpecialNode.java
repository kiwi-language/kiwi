package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;

import java.util.function.Consumer;

@Entity
public class InvokeSpecialNode extends InvokeNode {

    public InvokeSpecialNode(String name,
                             Node prev,
                             Code code,
                             MethodRef methodRef) {
        super(name, prev, code, methodRef);
    }

    public static Node read(CodeInput input, String name) {
        return new InvokeSpecialNode(name, input.getPrev(), input.getCode(), (MethodRef) input.readConstant());
    }

    @Override
    public MethodRef getFlowRef() {
        return (MethodRef) super.getFlowRef();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("invokespecial " + getFlowRef());
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.INVOKE_SPECIAL);
        writeCallCode(output);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitInvokeSpecialNode(this);
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
