package tech.metavm.object.instance.query;

import tech.metavm.object.instance.Instance;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListTree extends NTree {

    private final List<Instance> idList;
    private final List<NTree> children = new ArrayList<>();

    protected ListTree(Path path, List<Instance> idList) {
        super(path);
        this.idList = idList;
        for (Instance id : idList) {
            children.add(NTree.create(path, id));
        }
    }

    public List<Instance> getElements() {
        return idList;
    }

    @Override
    public List<NTree> getChildren() {
        return children;
    }

    @Override
    public List<Object> getValue() {
        return NncUtils.map(children, NTree::getValue);
    }

    public Object getFieldValue(String fieldPath) {
        List<Object> results = new ArrayList<>();
        for (NTree child : children) {
            results.add(child.getFieldValue(fieldPath));
        }
        return results;
    }

    @Override
    public void load() {
        children.forEach(NTree::load);
    }

}
