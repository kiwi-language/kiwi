package org.metavm.flow;

import org.metavm.entity.ElementVisitor;
import org.metavm.api.EntityType;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.LambdaNodeRefDTO;

import java.util.Objects;

@EntityType
public class LambdaNodeRef extends CallableRef  {

    private final LambdaNode lambdaNode;

    public LambdaNodeRef(LambdaNode lambdaNode) {
        this.lambdaNode = lambdaNode;
    }

    @Override
    protected boolean equals0(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LambdaNodeRef that)) return false;
        return Objects.equals(lambdaNode, that.lambdaNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lambdaNode);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLambdaNodeRef(this);
    }

    @Override
    public LambdaNodeRefDTO toDTO(SerializeContext serializeContext) {
        return new LambdaNodeRefDTO(serializeContext.getStringId(lambdaNode));
    }

    @Override
    public Callable resolve() {
        return lambdaNode;
    }

}
