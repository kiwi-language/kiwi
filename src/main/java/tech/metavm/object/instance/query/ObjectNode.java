package tech.metavm.object.instance.query;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ObjectNode extends InstanceNode<ClassInstance> {

    private final ClassType type;
    private final Map<String, InstanceNode<?>> children = new LinkedHashMap<>();

    public ObjectNode(PathTree path, ClassType type) {
        super(path);
        this.type = type;
        for (PathTree child : path.getChildren()) {
            children.put(
                    child.getName(),
                    InstanceNode.create(child, type.tryGetFieldByName(child.getName()).getType())
            );
        }
    }

    public List<NodeInstancePair> getNodeInstancePairsForChildren0(ClassInstance instance) {
        return NncUtils.map(
                getChildren(),
                child -> new NodeInstancePair(child, instance.getField(child.getName()))
        );
    }

    @Override
    public List<InstanceNode<?>> getChildren() {
        return new ArrayList<>(children.values());
    }

    @Override
    public Instance getByPath0(ClassInstance instance, Path path) {
        InstanceNode<?> child = children.get(path.firstItem());
        Instance fieldValue = instance.getField(path.firstItem());
        return child.getByPath(fieldValue, path.subPath());
    }

    @Override
    protected void fetch0(ClassInstance instance, Path path, List<Instance> result) {
        children.get(path.firstItem()).fetch(instance.getField(path.firstItem()), path.subPath(), result);
    }

    @Override
    protected Class<ClassInstance> getInstanceClass() {
        return ClassInstance.class;
    }
}
