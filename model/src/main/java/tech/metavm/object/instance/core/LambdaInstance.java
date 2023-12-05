package tech.metavm.object.instance.core;

import tech.metavm.flow.FlowExecResult;
import tech.metavm.flow.LambdaNode;
import tech.metavm.flow.MetaFrame;
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

    private MetaFrame createFrame(List<Instance> arguments, IInstanceContext context) {
        return new LambdaFrame(
                Objects.requireNonNull(lambdaNode.getBodyScope().tryGetFirstNode()),
                lambdaNode.getFlow().getDeclaringType(),
                arguments, context, containingFrame
        );
    }

    @Override
    public void accept(InstanceVisitor visitor) {
        visitor.visitLambdaInstance(this);
    }

    @Override
    public FlowExecResult execute(List<Instance> arguments, IInstanceContext context) {
        return createFrame(arguments, context).execute();
    }
}
