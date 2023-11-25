package tech.metavm.flow;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.type.Type;

public abstract class NonTerminatingNode<P> extends NodeRT<P> {


    protected NonTerminatingNode(Long tmpId, String name, @Nullable Type outputType, NodeRT<?> previous, ScopeRT scope) {
        super(tmpId, name, outputType, previous, scope);
    }
}
