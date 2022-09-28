package tech.metavm.object.instance.query;

import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListTree extends NTree {

    private final List<Long> idList;
    private final List<ObjectTree> children = new ArrayList<>();

    protected ListTree(Path path, List<Long> idList) {
        super(path);
        this.idList = idList;
        for (Long id : idList) {
            children.add(new ObjectTree(path, id));
        }
    }

    public List<Long> getIdList() {
        return idList;
    }

    @Override
    public List<ObjectTree> getChildObjectTrees() {
        return children;
    }

    @Override
    public List<Map<String, Object>> getValue() {
        return NncUtils.map(children, ObjectTree::getValue);
    }

    public Object getFieldValue(String fieldPath) {
        List<Object> results = new ArrayList<>();
        for (ObjectTree child : children) {
            results.add(child.getFieldValue(fieldPath));
        }
        return results;
    }

}
