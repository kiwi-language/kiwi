package tech.metavm.object.instance.query;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.PrimitiveInstance;
import tech.metavm.object.meta.PrimitiveType;

import java.util.List;

public class PrimitiveNode extends InstanceNode<PrimitiveInstance> {

    private final PrimitiveType type;

    public PrimitiveNode(PathTree path, PrimitiveType type) {
        super(path);
        this.type = type;
    }

    @Override
    protected Instance getByPath0(PrimitiveInstance instance, Path path) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void fetch0(PrimitiveInstance instance, Path path, List<Instance> result) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<NodeInstancePair> getNodeInstancePairsForChildren0(PrimitiveInstance instance) {
        return List.of();
    }

    @Override
    public List<InstanceNode<?>> getChildren() {
        return List.of();
    }

    @Override
    protected Class<PrimitiveInstance> getInstanceClass() {
        return PrimitiveInstance.class;
    }
}
