package tech.metavm.object.instance.core;

import tech.metavm.flow.FlowStack;
import tech.metavm.flow.Frame;
import tech.metavm.flow.LambdaNode;
import tech.metavm.flow.MetaFrame;
import tech.metavm.object.instance.LambdaFrame;

import java.util.List;

public class LambdaInstance extends FunctionInstance {

    private final LambdaNode lambdaNode;
    private final MetaFrame containingFrame;

    public LambdaInstance(LambdaNode lambdaNode, MetaFrame containingFrame) {
        super(lambdaNode.getFunctionType());
        this.lambdaNode = lambdaNode;
        this.containingFrame = containingFrame;
    }

    public Frame createFrame(FlowStack stack, List<Instance> arguments) {
        return new LambdaFrame(
                lambdaNode.getBodyScope().getFirstNode(),
                lambdaNode.getFlow().getDeclaringType(),
                arguments, stack, containingFrame
        );
    }

    @Override
    public void accept(InstanceVisitor visitor) {
        visitor.visitLambdaInstance(this);
    }
}
