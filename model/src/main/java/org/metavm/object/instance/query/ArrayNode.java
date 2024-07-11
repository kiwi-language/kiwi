package org.metavm.object.instance.query;

import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceReference;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Type;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArrayNode extends InstanceNode<InstanceReference> {

    private final Type type;
    private final Map<String, InstanceNode<?>> children = new HashMap<>();

    protected ArrayNode(PathTree path, ArrayType arrayType) {
        super(path);
        this.type = arrayType;
        for (PathTree child : path.getChildren()) {
            if(Path.isAsteriskItem(child.getName()) || Path.isIndexItem(child.getName())) {
                this.children.put(child.getName(), InstanceNode.create(child, arrayType.getElementType()));
            }
            else {
                throw new InternalException("Invalid array child name '" + child.getName() + "'");
            }
        }
    }

    @Override
    public List<InstanceNode<?>> getChildren() {
        return new ArrayList<>(children.values());
    }

    @Override
    public Instance getByPath0(InstanceReference instance, Path path) {
        InstanceNode<?> child = children.get(path.firstItem());
        if(child.isAsterisk()) {
            return Instances.createArray(
                    NncUtils.map(
                            instance.resolveArray().getElements(),
                            e -> child.getByPath(e, path.subPath())
                    )
            ).getReference();
        }
        else {
            return child.getByPath(instance.resolveArray().get(Integer.parseInt(path.firstItem())), path.subPath());
        }
    }

    @Override
    protected void fetch0(InstanceReference instance, Path path, List<Instance> result) {
        InstanceNode<?> child = children.get(path.firstItem());
        if(child.isAsterisk()) {
            for (Instance element : instance.resolveArray().getElements()) {
                child.fetch(element, path.subPath(), result);
            }
        }
        else {
            child.fetch(instance.resolveArray().getInstance(child.getNameAsIndex()), path.subPath(), result);
        }
    }

    @Override
    public List<NodeInstancePair> getNodeInstancePairsForChildren0(InstanceReference instance) {
        List<NodeInstancePair> pairs = new ArrayList<>();
        for (InstanceNode<?> child : children.values()) {
            if(child.isAsterisk()) {
                for (Instance element : instance.resolveArray().getElements()) {
                    pairs.add(new NodeInstancePair(child, element));
                }
            }
            else {
                pairs.add(new NodeInstancePair(child, instance.resolveArray().get(child.getNameAsIndex())));
            }
        }
        return pairs;
    }

    @Override
    protected Class<InstanceReference> getInstanceClass() {
        return InstanceReference.class;
    }

}
