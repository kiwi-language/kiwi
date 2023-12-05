package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.ChildEntity;
import tech.metavm.object.type.ClassType;
import tech.metavm.util.NncUtils;

public abstract class ChildTypeNode<P> extends NodeRT<P> {

    @ChildEntity("节点类型")
    private final ClassType nodeType;

    protected ChildTypeNode(Long tmpId, String name, ClassType outputType, NodeRT<?> previous, ScopeRT scope) {
        super(tmpId, name, null, previous, scope);
        this.nodeType = addChild(outputType, "nodeType");
    }

    @NotNull
    @Override
    public final ClassType getType() {
        return nodeType;
    }
}
