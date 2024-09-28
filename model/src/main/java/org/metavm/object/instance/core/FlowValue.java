package org.metavm.object.instance.core;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.natives.CallContext;
import org.metavm.flow.Flow;
import org.metavm.flow.FlowExecResult;
import org.metavm.flow.Flows;
import org.metavm.util.InstanceOutput;

import javax.annotation.Nullable;
import java.util.List;

@Slf4j
public class FlowValue extends FunctionValue {

    private final Flow flow;
    @Nullable
    private final ClassInstance boundSelf;

    public FlowValue(Flow flow, @Nullable ClassInstance boundSelf) {
        super(!Flows.isInstanceMethod(flow) || boundSelf != null ? flow.getType() : Flows.getStaticType(flow));
        this.flow = flow;
        this.boundSelf = boundSelf;
    }

    @Override
    public FlowExecResult execute(List<? extends Value> arguments, CallContext callContext) {
        if(boundSelf != null)
            return flow.execute(boundSelf, arguments, callContext);
        else
            return flow.execute(arguments.get(0).resolveObject(), arguments.subList(1, arguments.size()), callContext);
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
    public void write(InstanceOutput output) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R> R accept(ValueVisitor<R> visitor) {
        return visitor.visitFlowInstance(this);
    }

}
