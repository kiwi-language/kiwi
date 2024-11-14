package org.metavm.object.instance.core;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.natives.CallContext;
import org.metavm.flow.ClosureContext;
import org.metavm.flow.FlowExecResult;
import org.metavm.flow.Lambda;
import org.metavm.flow.MetaFrame;
import org.metavm.util.InstanceOutput;
import org.metavm.util.InternalException;

import java.util.List;

@Slf4j
public class LambdaValue extends FunctionValue {

    private final Lambda lambda;
    private final ClosureContext closureContext;

    public LambdaValue(Lambda lambda, ClosureContext closureContext) {
        super(lambda.getFunctionType());
        this.lambda = lambda;
        this.closureContext = closureContext;
    }

    private MetaFrame createFrame(InstanceRepository instanceRepository) {
        return new MetaFrame(instanceRepository);
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
            return createFrame(callContext.instanceRepository()).execute(
                    lambda.getScope(),
                    arguments.toArray(Value[]::new),
                    closureContext
            );
        } catch (Exception e) {
            log.info("Fail to execute lambda");
            log.info(lambda.getText());
            throw new InternalException("fail to lambda", e);
        }
    }

}
