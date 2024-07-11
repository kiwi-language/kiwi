package org.metavm.object.instance.query;

import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceReference;
import org.metavm.object.type.Klass;
import org.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ObjectNode extends InstanceNode<InstanceReference> {

    private final Klass type;
    private final Map<String, InstanceNode<?>> children = new LinkedHashMap<>();

    public ObjectNode(PathTree path, Klass type) {
        super(path);
        this.type = type;
        for (PathTree child : path.getChildren()) {
            children.put(
                    child.getName(),
                    InstanceNode.create(child, type.tryGetFieldByName(child.getName()).getType())
            );
        }
    }

    @Override
    public List<NodeInstancePair> getNodeInstancePairsForChildren0(InstanceReference instance) {
        return NncUtils.map(
                getChildren(),
                child -> new NodeInstancePair(child, instance.resolveObject().getField(child.getName()))
        );
    }

    @Override
    public List<InstanceNode<?>> getChildren() {
        return new ArrayList<>(children.values());
    }

    @Override
    public Instance getByPath0(InstanceReference instance, Path path) {
        InstanceNode<?> child = children.get(path.firstItem());
        Instance fieldValue = instance.resolveObject().getField(path.firstItem());
        return child.getByPath(fieldValue, path.subPath());
    }

    @Override
    protected void fetch0(InstanceReference instance, Path path, List<Instance> result) {
        children.get(path.firstItem()).fetch(instance.resolveObject().getField(path.firstItem()), path.subPath(), result);
    }

    @Override
    protected Class<InstanceReference> getInstanceClass() {
        return InstanceReference.class;
    }
}
