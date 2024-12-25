package org.metavm.object.instance.core;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.natives.CallContext;
import org.metavm.entity.natives.DefaultCallContext;
import org.metavm.flow.*;
import org.metavm.object.type.FunctionType;
import org.metavm.object.type.TypeMetadata;
import org.metavm.util.InstanceOutput;
import org.metavm.util.InternalException;
import org.metavm.util.MvOutput;

import java.util.List;

@Slf4j
public class LambdaValue extends FunctionValue {

    private final LambdaRef lambdaRef;
    private final ClosureContext closureContext;

    public LambdaValue(LambdaRef lambdaRef, ClosureContext closureContext) {
        this.lambdaRef = lambdaRef;
        this.closureContext = closureContext;
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
            return VmStack.execute(
                    lambdaRef,
                    arguments.toArray(Value[]::new),
                    closureContext,
                    new DefaultCallContext(callContext.instanceRepository())
            );
        } catch (Exception e) {
            log.info("Fail to execute lambda");
            log.info(lambdaRef.getRawLambda().getText());
            throw new InternalException("fail to lambda", e);
        }
    }

    @Override
    public FunctionType getType() {
        return lambdaRef.getFunctionType();
    }

    @Override
    public Code getCode() {
        return lambdaRef.getRawLambda().getCode();
    }

    @Override
    public FlowRef getFlow() {
        return lambdaRef.getFlow();
    }

    @Override
    public TypeMetadata getTypeMetadata() {
        return lambdaRef.getTypeMetadata();
    }

    @Override
    public ClosureContext getClosureContext(Value[] stack, int base) {
        return closureContext;
    }

}
