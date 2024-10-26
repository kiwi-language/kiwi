package org.metavm.object.instance.core;

import org.metavm.entity.natives.CallContext;
import org.metavm.flow.FlowExecResult;
import org.metavm.flow.Flows;
import org.metavm.flow.LambdaEnterNode;
import org.metavm.flow.MetaFrame;
import org.metavm.object.instance.LambdaFrame;
import org.metavm.util.InstanceOutput;

import java.util.List;
import java.util.Objects;

public class Lambda extends FunctionValue {

    private final LambdaEnterNode lambdaEnterNode;
    private final MetaFrame containingFrame;

    public Lambda(LambdaEnterNode lambdaEnterNode, MetaFrame containingFrame) {
        super(lambdaEnterNode.getFunctionType());
        this.lambdaEnterNode = lambdaEnterNode;
        this.containingFrame = containingFrame;
    }

    private MetaFrame createFrame(List<? extends Value> arguments, InstanceRepository instanceRepository) {
        return new LambdaFrame(
                Objects.requireNonNull(lambdaEnterNode.getSuccessor()),
                Flows.getDeclaringType(lambdaEnterNode.getFlow()),
                arguments, instanceRepository, containingFrame
        );
    }

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
        return visitor.visitLambdaInstance(this);
    }

    @Override
    public FlowExecResult execute(List<? extends Value> arguments, CallContext callContext) {
        return createFrame(arguments, callContext.instanceRepository()).execute();
    }
}
