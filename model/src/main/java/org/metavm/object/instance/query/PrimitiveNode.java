package org.metavm.object.instance.query;

import org.metavm.object.instance.core.PrimitiveValue;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.PrimitiveType;

import java.util.List;

public class PrimitiveNode extends InstanceNode<PrimitiveValue> {

    private final PrimitiveType type;

    public PrimitiveNode(PathTree path, PrimitiveType type) {
        super(path);
        this.type = type;
    }

    @Override
    protected Value getByPath0(PrimitiveValue instance, Path path) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void fetch0(PrimitiveValue instance, Path path, List<Value> result) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<NodeInstancePair> getNodeInstancePairsForChildren0(PrimitiveValue instance) {
        return List.of();
    }

    @Override
    public List<InstanceNode<?>> getChildren() {
        return List.of();
    }

    @Override
    protected Class<PrimitiveValue> getInstanceClass() {
        return PrimitiveValue.class;
    }
}
