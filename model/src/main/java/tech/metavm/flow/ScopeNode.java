package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.object.type.Type;

import javax.annotation.Nullable;

@EntityType("范围节点")
public abstract class ScopeNode extends NodeRT {

    @ChildEntity("范围")
    protected final ScopeRT bodyScope;

    protected ScopeNode(Long tmpId, String name, @Nullable String code, @Nullable Type outputType, NodeRT previous, ScopeRT scope, boolean scopeWithBackEdge) {
        super(tmpId, name, code, outputType, previous, scope);
        bodyScope = addChild(new ScopeRT(scope.getFlow(), this, scopeWithBackEdge), "bodyScope");
    }

    public ScopeRT getBodyScope() {
        return bodyScope;
    }

}
