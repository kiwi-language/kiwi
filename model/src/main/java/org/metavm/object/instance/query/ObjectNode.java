package org.metavm.object.instance.query;

import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Klass;
import org.metavm.util.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ObjectNode extends InstanceNode<Reference> {

    private final Klass type;
    private final Map<String, InstanceNode<?>> children = new LinkedHashMap<>();

    public ObjectNode(PathTree path, Klass type) {
        super(path);
        this.type = type;
        for (PathTree child : path.getChildren()) {
            children.put(
                    child.getName(),
                    InstanceNode.create(child, type.findFieldByName(child.getName()).getType())
            );
        }
    }

    @Override
    public List<NodeInstancePair> getNodeInstancePairsForChildren0(Reference instance) {
        return Utils.map(
                getChildren(),
                child -> new NodeInstancePair(child, instance.resolveObject().getField(child.getName()))
        );
    }

    @Override
    public List<InstanceNode<?>> getChildren() {
        return new ArrayList<>(children.values());
    }

    @Override
    public Value getByPath0(Reference instance, Path path) {
        InstanceNode<?> child = children.get(path.firstItem());
        Value fieldValue = instance.resolveObject().getField(path.firstItem());
        return child.getByPath(fieldValue, path.subPath());
    }

    @Override
    protected void fetch0(Reference instance, Path path, List<Value> result) {
        children.get(path.firstItem()).fetch(instance.resolveObject().getField(path.firstItem()), path.subPath(), result);
    }

    @Override
    protected Class<Reference> getInstanceClass() {
        return Reference.class;
    }
}
