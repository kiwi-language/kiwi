package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public abstract class GenericInvokeNode extends InvokeNode {

    public static final Logger logger = LoggerFactory.getLogger(GenericInvokeNode.class);

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

}
