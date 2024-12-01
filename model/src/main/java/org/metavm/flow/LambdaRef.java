package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.FunctionType;
import org.metavm.object.type.TypeMetadata;
import org.metavm.util.WireTypes;

import java.util.Objects;

@EntityType
public class LambdaRef extends CallableRef  {

    public static LambdaRef read(KlassInput input) {
        return new LambdaRef(
                (FlowRef) input.readElement(),
                input.getLambda(input.readId()));
    }

    private final FlowRef flowRef;
    private final Lambda rawLambda;

    public LambdaRef(FlowRef flowRef, Lambda rawLambda) {
        this.flowRef = flowRef;
        this.rawLambda = rawLambda;
    }

    @Override
    protected boolean equals0(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LambdaRef that)) return false;
        return Objects.equals(rawLambda, that.rawLambda);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawLambda);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLambdaRef(this);
    }

    @Override
    public TypeMetadata getTypeMetadata() {
        return flowRef.getTypeMetadata();
    }

    public void write(KlassOutput output) {
        output.write(WireTypes.LAMBDA_REF);
        flowRef.write(output);
        output.writeEntityId(rawLambda);
    }

    public FunctionType getFunctionType() {
        return flowRef.getTypeMetadata().getFunctionType(rawLambda.getTypeIndex());
    }

    public FlowRef getFlowRef() {
        return flowRef;
    }

    public Lambda getRawLambda() {
        return rawLambda;
    }

    @Override
    protected String toString0() {
        return "LambdaRef " + flowRef + ".<lambda>";
    }
}
