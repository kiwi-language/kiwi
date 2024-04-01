package tech.metavm.object.instance.core;

import tech.metavm.entity.natives.CallContext;
import tech.metavm.flow.*;
import tech.metavm.object.instance.LambdaFrame;
import tech.metavm.object.type.CompositeTypeFacade;

import java.util.List;
import java.util.Objects;

public class LambdaInstance extends FunctionInstance {

    private final LambdaNode lambdaNode;
    private final MetaFrame containingFrame;

    public LambdaInstance(LambdaNode lambdaNode, MetaFrame containingFrame) {
        super(lambdaNode.getFunctionType());
        this.lambdaNode = lambdaNode;
        this.containingFrame = containingFrame;
    }

    private MetaFrame createFrame(List<Instance> arguments, InstanceRepository instanceRepository, ParameterizedFlowProvider parameterizedFlowProvider, CompositeTypeFacade compositeTypeFacade) {
        return new LambdaFrame(
                Objects.requireNonNull(lambdaNode.getBodyScope().tryGetFirstNode()),
                Flows.getDeclaringType(lambdaNode.getFlow()),
                arguments, instanceRepository, parameterizedFlowProvider, compositeTypeFacade, containingFrame
        );
    }

    @Override
    public <R> R accept(InstanceVisitor<R> visitor) {
        return visitor.visitLambdaInstance(this);
    }

    @Override
    public FlowExecResult execute(List<Instance> arguments, CallContext callContext) {
        return createFrame(arguments, callContext.instanceRepository(), callContext.parameterizedFlowProvider(), callContext.compositeTypeFacade()).execute();
    }
}
