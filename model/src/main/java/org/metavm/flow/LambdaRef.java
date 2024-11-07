package org.metavm.flow;

import org.metavm.entity.ElementVisitor;
import org.metavm.api.EntityType;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.LambdaRefDTO;

import java.util.Objects;

@EntityType
public class LambdaRef extends CallableRef  {

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
    public LambdaRefDTO toDTO(SerializeContext serializeContext) {
        return new LambdaRefDTO(serializeContext.getStringId(lambda));
    }

    @Override
    public Lambda resolve() {
        return lambda;
    }

}
