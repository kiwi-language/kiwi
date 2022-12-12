package tech.metavm.object.instance.query;

import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ArrayInstance;
import tech.metavm.object.instance.PrimitiveInstance;
import tech.metavm.util.InternalException;

import java.util.List;

public abstract class NTree {

    protected final Path path;

    protected NTree(Path path) {
        this.path = path;
    }

    public final String getName() {
        return path.getName();
    }

    public final Path getPath() {
        return path;
    }

    public abstract List<NTree> getChildren();

    public abstract Object getValue();

    public static NTree create(Path path, Instance instance) {
        if(instance instanceof ClassInstance classInstance) {
            return new ObjectTree(path, classInstance);
        }
        if(instance instanceof ArrayInstance array) {
            return new ListTree(path, array.getElements());
        }
        if(instance instanceof PrimitiveInstance primitiveInstance) {
            return new ValueTree(path, primitiveInstance.getValue());
        }
        throw new InternalException("Can not create tree for instance " + instance);
    }

    protected abstract Object getFieldValue(String fieldPath);

    public abstract void load();

}
