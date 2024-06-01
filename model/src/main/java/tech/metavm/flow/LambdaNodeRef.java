package tech.metavm.flow;

import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.SerializeContext;
import tech.metavm.flow.rest.LambdaNodeRefDTO;

import java.util.Objects;

@EntityType("LambdaNodeRef")
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
