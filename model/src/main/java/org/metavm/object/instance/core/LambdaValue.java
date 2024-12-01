package org.metavm.object.instance.core;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.natives.CallContext;
import org.metavm.flow.*;
import org.metavm.object.type.ConstantPool;
import org.metavm.util.InstanceOutput;
import org.metavm.util.InternalException;
import org.metavm.util.MvOutput;

import java.util.List;

@Slf4j
public class LambdaValue extends FunctionValue {

    private final LambdaRef lambdaRef;
    private final ClosureContext closureContext;

    public LambdaValue(LambdaRef lambdaRef, ClosureContext closureContext) {
        super(lambdaRef.getFunctionType());
        this.lambdaRef = lambdaRef;
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
    public void write(MvOutput output) {
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
                    lambdaRef.getRawLambda().getCode(),
                    arguments.toArray(Value[]::new),
                    lambdaRef.getTypeMetadata(),
                    closureContext
            );
        } catch (Exception e) {
            log.info("Fail to execute lambda");
            log.info(lambdaRef.getRawLambda().getText());
            throw new InternalException("fail to lambda", e);
        }
    }

}
