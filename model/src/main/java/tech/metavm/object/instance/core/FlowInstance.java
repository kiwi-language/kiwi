package tech.metavm.object.instance.core;

import tech.metavm.flow.*;

import javax.annotation.Nullable;
import java.util.List;

public class FlowInstance extends FunctionInstance {

    private final Flow flow;
    @Nullable
    private final ClassInstance boundSelf;

    public FlowInstance(Flow flow, @Nullable ClassInstance boundSelf) {
        super(boundSelf != null ? flow.getType() : flow.getStaticType());
        this.flow = flow;
        this.boundSelf = boundSelf;
    }

    @Override
    public FlowExecResult execute(List<Instance> arguments, IInstanceContext context) {
        if(boundSelf != null)
            return flow.execute(boundSelf, arguments, context);
        else
            return flow.execute(arguments.get(0), arguments.subList(0, arguments.size()), context);
    }

//    public Frame createFrame(FlowStack stack, List<Instance> arguments) {
//        var self = boundSelf != null ? boundSelf : arguments.get(0);
//        var actualArgs = boundSelf != null ? arguments : arguments.subList(1, arguments.size());
//        return flow.isNative() ? new NativeFrame(flow, self, arguments) :
//                new MetaFrame(flow, self, actualArgs, stack);
//    }

    @Override
    public void accept(InstanceVisitor visitor) {
        visitor.visitFlowInstance(this);
    }
}
