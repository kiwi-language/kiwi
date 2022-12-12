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
    protected Object getFieldValue(String fieldPath) {
        return value;
    }

    @Override
    public void load() {

    }

    @Override
    public List<NTree> getChildren() {
        return List.of();
    }

}
