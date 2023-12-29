package tech.metavm.object.instance.core;

import tech.metavm.flow.*;
import tech.metavm.object.instance.LambdaFrame;

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

    private MetaFrame createFrame(List<Instance> arguments, InstanceRepository instanceRepository, ParameterizedFlowProvider parameterizedFlowProvider) {
        return new LambdaFrame(
                Objects.requireNonNull(lambdaNode.getBodyScope().tryGetFirstNode()),
                Flows.getDeclaringType(lambdaNode.getFlow()),
                arguments, instanceRepository, parameterizedFlowProvider, containingFrame
        );
    }

    @Override
    public <R> R accept(InstanceVisitor<R> visitor) {
        return visitor.visitLambdaInstance(this);
    }

    @Override
    public FlowExecResult execute(List<Instance> arguments, InstanceRepository instanceRepository, ParameterizedFlowProvider parameterizedFlowProvider) {
        return createFrame(arguments, instanceRepository, parameterizedFlowProvider).execute();
    }
}
