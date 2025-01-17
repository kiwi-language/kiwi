package org.metavm.object.instance.core;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.natives.CallContext;
import org.metavm.entity.natives.DefaultCallContext;
import org.metavm.flow.*;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.FunctionType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.TypeMetadata;
import org.metavm.util.InternalException;
import org.metavm.util.MvOutput;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class LambdaValue extends FunctionValue {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private final LambdaRef lambdaRef;
    private final ClosureContext closureContext;

    public LambdaValue(LambdaRef lambdaRef, ClosureContext closureContext) {
        this.lambdaRef = lambdaRef;
        this.closureContext = closureContext;
    }

    @Override
    public void writeInstance(MvOutput output) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(MvOutput output) {
        throw new UnsupportedOperationException();
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

    @Override
    public FunctionType getValueType() {
        return lambdaRef.getFunctionType();
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        lambdaRef.accept(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        lambdaRef.forEachReference(action);
    }
}
