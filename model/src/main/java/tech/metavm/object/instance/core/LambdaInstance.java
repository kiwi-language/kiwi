package tech.metavm.object.instance.core;

import tech.metavm.entity.natives.CallContext;
import tech.metavm.flow.FlowExecResult;
import tech.metavm.flow.Flows;
import tech.metavm.flow.LambdaNode;
import tech.metavm.flow.MetaFrame;
import tech.metavm.object.instance.LambdaFrame;
import tech.metavm.util.InstanceOutput;

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

    private MetaFrame createFrame(List<Instance> arguments, InstanceRepository instanceRepository) {
        return new LambdaFrame(
                Objects.requireNonNull(lambdaNode.getBodyScope().tryGetFirstNode()),
                Flows.getDeclaringType(lambdaNode.getFlow()),
                arguments, instanceRepository, containingFrame
        );
    }

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
        return visitor.visitLambdaInstance(this);
    }

    @Override
    public FlowExecResult execute(List<Instance> arguments, CallContext callContext) {
        return createFrame(arguments, callContext.instanceRepository()).execute();
    }
}
