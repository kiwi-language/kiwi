package org.metavm.flow;

import org.metavm.entity.ChildEntity;
import org.metavm.entity.EntityType;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;

@EntityType
public abstract class ScopeNode extends NodeRT {

    @ChildEntity
    protected final ScopeRT bodyScope;

    protected ScopeNode(Long tmpId, String name, @Nullable String code, @Nullable Type outputType, NodeRT previous, ScopeRT scope, boolean scopeWithBackEdge) {
        super(tmpId, name, code, outputType, previous, scope);
        bodyScope = addChild(new ScopeRT(scope.getFlow(), this, scopeWithBackEdge), "bodyScope");
    }

    public ScopeRT getBodyScope() {
        return bodyScope;
    }

}
