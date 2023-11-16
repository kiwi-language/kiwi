package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.ChildEntity;
import tech.metavm.object.type.ClassType;

public abstract class ChildTypeNode<P> extends NodeRT<P> {

    @ChildEntity("节点类型")
    private final ClassType nodeType;

    protected ChildTypeNode(Long tmpId, String name, @Nullable ClassType outputType, NodeRT<?> previous, ScopeRT scope) {
        super(tmpId, name, null, previous, scope);
        this.nodeType = outputType;
    }

    @NotNull
    @Override
    public final ClassType getType() {
        return nodeType;
    }
}
