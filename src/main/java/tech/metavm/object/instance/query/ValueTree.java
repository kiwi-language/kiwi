package tech.metavm.object.instance.query;

import java.util.List;

public class ValueTree extends NTree {

    private final Object value;

    public ValueTree(Path path, Object value) {
        super(path);
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public List<ObjectTree> getChildObjectTrees() {
        return List.of();
    }

}
