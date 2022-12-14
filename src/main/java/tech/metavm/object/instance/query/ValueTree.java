package tech.metavm.object.instance.query;

import tech.metavm.object.instance.Instance;

import java.util.List;

public class ValueTree extends NTree {

    private final Instance value;

    public ValueTree(Path path, Instance value) {
        super(path);
        this.value = value;
    }

    @Override
    public Instance getValue() {
        return value;
    }

    @Override
    protected Instance getFieldValue(String fieldPath) {
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
