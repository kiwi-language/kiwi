package org.metavm.object.instance.core;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.natives.CallContext;
import org.metavm.flow.FlowExecResult;
import org.metavm.flow.Flows;
import org.metavm.flow.Lambda;
import org.metavm.flow.MetaFrame;
import org.metavm.util.InstanceOutput;
import org.metavm.util.InternalException;

import java.util.List;
import java.util.Objects;

@Slf4j
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
        try {
            return createFrame(arguments, callContext.instanceRepository()).execute();
        } catch (Exception e) {
            log.info("Fail to execute lambda");
            log.info(lambda.getText());
            throw new InternalException("fail to lambda", e);
        }
    }

}
