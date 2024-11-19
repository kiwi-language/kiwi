package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.util.MvInput;
import org.metavm.util.WireTypes;

import java.util.Objects;

@EntityType
public class LambdaRef extends CallableRef  {

    public static LambdaRef read(MvInput input) {
        return new LambdaRef(input.getLambda(input.readId()));
    }

    private final Lambda lambda;

    public LambdaRef(Lambda lambda) {
        this.lambda = lambda;
    }

    @Override
    protected boolean equals0(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LambdaRef that)) return false;
        return Objects.equals(lambda, that.lambda);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lambda);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLambdaNodeRef(this);
    }

    @Override
    public Lambda resolve() {
        return lambda;
    }

    public void write(KlassOutput output) {
        output.write(WireTypes.LAMBDA_REF);
        output.writeEntityId(lambda);
    }
}
