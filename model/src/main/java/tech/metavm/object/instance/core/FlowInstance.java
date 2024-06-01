package tech.metavm.object.instance.core;

import tech.metavm.entity.natives.CallContext;
import tech.metavm.flow.Flow;
import tech.metavm.flow.FlowExecResult;
import tech.metavm.flow.Flows;
import tech.metavm.util.InstanceOutput;

import javax.annotation.Nullable;
import java.util.List;

public class FlowInstance extends FunctionInstance {

    private final Flow flow;
    @Nullable
    private final ClassInstance boundSelf;

    public FlowInstance(Flow flow, @Nullable ClassInstance boundSelf) {
        super(!Flows.isInstanceMethod(flow) || boundSelf != null ? flow.getType() : Flows.getStaticType(flow));
        this.flow = flow;
        this.boundSelf = boundSelf;
    }

    @Override
    public FlowExecResult execute(List<Instance> arguments, CallContext callContext) {
        if(boundSelf != null)
            return flow.execute(boundSelf, arguments, callContext);
        else
            return flow.execute((ClassInstance) arguments.get(0), arguments.subList(1, arguments.size()), callContext);
    }

//    public Frame createFrame(FlowStack stack, List<Instance> arguments) {
//        var self = boundSelf != null ? boundSelf : arguments.get(0);
//        var actualArgs = boundSelf != null ? arguments : arguments.subList(1, arguments.size());
//        return flow.isNative() ? new NativeFrame(flow, self, arguments) :
//                new MetaFrame(flow, self, actualArgs, stack);
//    }

    @Override
    public void writeRecord(InstanceOutput output) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(InstanceOutput output) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R> R accept(InstanceVisitor<R> visitor) {
        return visitor.visitFlowInstance(this);
    }
}
