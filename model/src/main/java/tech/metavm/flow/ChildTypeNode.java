package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.object.type.ClassType;

import javax.annotation.Nullable;

@EntityType("子类型节点")
public abstract class ChildTypeNode extends NodeRT {

    @ChildEntity("节点类型")
    private final ClassType nodeType;

    protected ChildTypeNode(Long tmpId, String name, @Nullable String code, ClassType outputType, NodeRT previous, ScopeRT scope) {
        super(tmpId, name, code, null, previous, scope);
        this.nodeType = addChild(outputType, "nodeType");
    }

    @NotNull
    @Override
    public final ClassType getType() {
        return nodeType;
    }
}
