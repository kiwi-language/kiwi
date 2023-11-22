package tech.metavm.object.instance.query;

import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.PrimitiveType;
import tech.metavm.object.type.Type;
import tech.metavm.util.InternalException;

import java.util.ArrayList;
import java.util.List;

public abstract class InstanceNode<I extends Instance> {

    protected final PathTree path;

    protected InstanceNode(PathTree path) {
        this.path = path;
    }

    public final String getName() {
        return path.getName();
    }

    public int getNameAsIndex() {
        return Path.parseIndexItem(getName());
    }

    public boolean isAsterisk() {
        return Path.isAsteriskItem(getName());
    }

    public final PathTree getPath() {
        return path;
    }

    public abstract List<InstanceNode<?>> getChildren();

    public final Instance getByPath(Instance instance, Path path) {
        if(path.isEmpty()) {
            return instance;
        }
        else {
            return getByPath0(tryCasting(instance), path);
        }
    }

    protected abstract Instance getByPath0(I instance, Path path);

    public final List<Instance> getFetchResults(Instance instance, Path path) {
        List<Instance> result = new ArrayList<>();
        fetch(instance, path, result);
        return result;
    }

    protected final void fetch(Instance instance, Path path, List<Instance> result) {
        if(path.isEmpty()) {
            result.add(instance);
        }
        else {
            fetch0(tryCasting(instance), path, result);
        }
    }

    protected abstract void fetch0(I instance, Path path, List<Instance> result);

    public static InstanceNode<?> create(PathTree path, Type type) {
        if(type.isBinaryNullable()) {
            type = type.getUnderlyingType();
        }
        if(type instanceof ClassType classType) {
            return new ObjectNode(path, classType);
        }
        if(type instanceof ArrayType arrayType) {
            return new ArrayNode(path, arrayType);
        }
        if(type instanceof PrimitiveType primitiveType) {
            return new PrimitiveNode(path, primitiveType);
        }
        throw new InternalException("Can not create tree for type " + type);
    }

    public List<NodeInstancePair>  getNodeInstancePairsForChildren(Instance instance) {
        return getNodeInstancePairsForChildren0(tryCasting(instance));
    }

    protected I tryCasting(Instance instance) {
        if(getInstanceClass().isInstance(instance)) {
            return getInstanceClass().cast(instance);
        }
        else {
            throw new InternalException("Expecting instance of type " + getInstanceClass().getName()
                    + ", but got " + instance);
        }
    }

    protected abstract List<NodeInstancePair> getNodeInstancePairsForChildren0(I instance);

    protected abstract Class<I> getInstanceClass();

}
