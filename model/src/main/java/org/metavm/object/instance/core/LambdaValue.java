package org.metavm.object.instance.core;

import org.metavm.entity.natives.CallContext;
import org.metavm.flow.*;
import org.metavm.util.InstanceOutput;

import java.util.List;
import java.util.Objects;

public class LambdaValue extends FunctionValue {

    private final Lambda lambda;
    private final MetaFrame containingFrame;

    public LambdaValue(Lambda lambda, MetaFrame containingFrame) {
        super(lambda.getFunctionType());
        this.lambda = lambda;
        this.containingFrame = containingFrame;
    }

    private MetaFrame createFrame(List<? extends Value> arguments, InstanceRepository instanceRepository) {
        return new MetaFrame(
                Objects.requireNonNull(lambda.getScope().tryGetFirstNode()),
                Flows.getDeclaringType(lambda.getScope().getFlow()),
                null,
                arguments, instanceRepository, containingFrame,
                lambda.getScope().getMaxLocals(), lambda.getScope().getMaxStack()
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
        return visitor.visitLambdaValue(this);
    }

    @Override
    public FlowExecResult execute(List<? extends Value> arguments, CallContext callContext) {
        return createFrame(arguments, callContext.instanceRepository()).execute();
    }
}
