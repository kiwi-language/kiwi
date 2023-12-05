package tech.metavm.flow;

import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.rest.ScopeNodeParamDTO;
import tech.metavm.object.type.Type;

@EntityType("范围节点")
public abstract class ScopeNode<P extends ScopeNodeParamDTO> extends NodeRT<P> {

    @ChildEntity("下属范围")
    protected final ScopeRT bodyScope;

    protected ScopeNode(Long tmpId, String name, @Nullable Type outputType, NodeRT<?> previous, ScopeRT scope, boolean scopeWithBackEdge) {
        super(tmpId, name, outputType, previous, scope);
        bodyScope = addChild(new ScopeRT(scope.getFlow(), this, scopeWithBackEdge), "bodyScope");
    }

    public ScopeRT getBodyScope() {
        return bodyScope;
    }

}
