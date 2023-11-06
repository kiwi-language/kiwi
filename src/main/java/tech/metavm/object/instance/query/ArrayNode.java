package tech.metavm.object.instance.query;

import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.meta.ArrayType;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.meta.Type;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArrayNode extends InstanceNode<ArrayInstance> {

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
    public Instance getByPath0(ArrayInstance instance, Path path) {
        InstanceNode<?> child = children.get(path.firstItem());
        if(child.isAsterisk()) {
            return InstanceUtils.createArray(
                    NncUtils.map(
                            instance.getElements(),
                            e -> child.getByPath(e, path.subPath())
                    )
            );
        }
        else {
            return child.getByPath(instance.get(Integer.parseInt(path.firstItem())), path.subPath());
        }
    }

    @Override
    protected void fetch0(ArrayInstance instance, Path path, List<Instance> result) {
        InstanceNode<?> child = children.get(path.firstItem());
        if(child.isAsterisk()) {
            for (Instance element : instance.getElements()) {
                child.fetch(element, path.subPath(), result);
            }
        }
        else {
            child.fetch(instance.getInstance(child.getNameAsIndex()), path.subPath(), result);
        }
    }

    @Override
    public List<NodeInstancePair> getNodeInstancePairsForChildren0(ArrayInstance instance) {
        List<NodeInstancePair> pairs = new ArrayList<>();
        for (InstanceNode<?> child : children.values()) {
            if(child.isAsterisk()) {
                for (Instance element : instance.getElements()) {
                    pairs.add(new NodeInstancePair(child, element));
                }
            }
            else {
                pairs.add(new NodeInstancePair(child, instance.get(child.getNameAsIndex())));
            }
        }
        return pairs;
    }

    @Override
    protected Class<ArrayInstance> getInstanceClass() {
        return ArrayInstance.class;
    }

}
