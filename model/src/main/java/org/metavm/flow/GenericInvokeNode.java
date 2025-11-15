package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;

import java.util.function.Consumer;

@Entity
public abstract class GenericInvokeNode extends InvokeNode {

    public GenericInvokeNode(String name, Node prev, Code code, @NotNull FlowRef flowRef) {
        super(name, prev, code, flowRef);
    }

    @Override
    public int getStackChange() {
        var flowRef = getFlowRef();
        if(flowRef.getReturnType().isVoid())
            return -flowRef.getRawFlow().getInputCount() - flowRef.getRawFlow().getTypeInputCount();
        else
            return 1 - flowRef.getRawFlow().getInputCount() - flowRef.getRawFlow().getTypeInputCount();
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
