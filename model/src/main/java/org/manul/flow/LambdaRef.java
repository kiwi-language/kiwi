package org.manul.flow;

import lombok.Getter;
import org.manul.api.Entity;
import org.manul.entity.ElementVisitor;
import org.manul.entity.Writable;
import org.manul.object.instance.core.Reference;
import org.manul.object.type.FunctionType;
import org.manul.object.type.TypeMetadata;
import org.manul.util.MvInput;
import org.manul.util.MvOutput;
import org.manul.util.WireTypes;

import java.util.Objects;
import java.util.function.Consumer;

@Entity
public class LambdaRef implements Writable, CallableRef {

    public static LambdaRef read(MvInput input) {
        return new LambdaRef((FlowRef) input.readValue(), input.readReference());
    }

    @Getter
    private final FlowRef flowRef;
//    public Lambda rawLambda;
    private final Reference lambdaReference;

    public LambdaRef(FlowRef flowRef, Lambda rawLambda) {
        this(flowRef, rawLambda.getReference());
    }

    private LambdaRef(FlowRef flowRef, Reference lambdaReference) {
        this.flowRef = flowRef;
        this.lambdaReference = lambdaReference;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LambdaRef that)) return false;
        return Objects.equals(lambdaReference, that.lambdaReference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lambdaReference);
    }

    @Override
    public TypeMetadata getTypeMetadata() {
        return flowRef.getTypeMetadata();
    }

    @Override
    public Code getCode() {
        return getRawLambda().getCode();
    }

    @Override
    public FlowRef getFlow() {
        return flowRef;
    }


    public void write(MvOutput output) {
        output.write(WireTypes.LAMBDA_REF);
        flowRef.write(output);
        output.writeReference(lambdaReference);
    }

    public FunctionType getFunctionType() {
        return getRawLambda().getType(flowRef.getTypeMetadata());
    }

    public Lambda getRawLambda() {
        return (Lambda) lambdaReference.get();
    }

    @Override
    public String toString() {
        return "LambdaRef " + flowRef + ".<lambda>";
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLambdaRef(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        flowRef.accept(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        flowRef.forEachReference(action);
        action.accept(lambdaReference);
    }
}
