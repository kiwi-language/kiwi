package org.metavm.object.instance.core;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.natives.CallContext;
import org.metavm.flow.FlowExecResult;
import org.metavm.flow.FlowRef;
import org.metavm.flow.Flows;
import org.metavm.object.type.FunctionType;
import org.metavm.util.InstanceOutput;
import org.metavm.util.MvOutput;

import javax.annotation.Nullable;
import java.util.List;

@Slf4j
public class FlowValue extends FunctionValue {

    private final FlowRef flow;
    @Nullable
    private final ClassInstance boundSelf;

    public FlowValue(FlowRef flow, @Nullable ClassInstance boundSelf) {
        this.flow = flow;
        this.boundSelf = boundSelf;
    }

    @Override
    public FlowExecResult execute(List<? extends Value> arguments, CallContext callContext) {
        if(boundSelf != null)
            return flow.execute(boundSelf.getReference(), arguments, callContext);
        else
            return flow.execute(arguments.get(0), arguments.subList(1, arguments.size()), callContext);
    }

//    public Frame createFrame(FlowStack stack, List<Instance> arguments) {
//        var self = boundSelf != null ? boundSelf : arguments.get(0);
//        var actualArgs = boundSelf != null ? arguments : arguments.subList(1, arguments.size());
//        return flow.isNative() ? new NativeFrame(flow, self, arguments) :
//                new MetaFrame(flow, self, actualArgs, stack);
//    }

    @Override
    public void writeInstance(InstanceOutput output) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(MvOutput output) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R> R accept(ValueVisitor<R> visitor) {
        return visitor.visitFlowValue(this);
    }

    @Nullable
    public ClassInstance getBoundSelf() {
        return boundSelf;
    }

    public FlowRef getFlow() {
        return flow;
    }

    @Override
    public FunctionType getType() {
        return !Flows.isInstanceMethod(flow.getRawFlow()) || boundSelf != null ? flow.getType() : Flows.getStaticType(flow);
    }
}
