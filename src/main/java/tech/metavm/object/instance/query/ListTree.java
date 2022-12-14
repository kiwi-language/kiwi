package tech.metavm.object.instance.query;

import tech.metavm.object.instance.ArrayInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.util.InstanceUtils;

import java.util.ArrayList;
import java.util.List;

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
    public ArrayInstance getValue() {
//        return NncUtils.map(children, NTree::getValue);
        return InstanceUtils.createArray(idList);
    }

    public Instance getFieldValue(String fieldPath) {
        ArrayInstance arrayInstance = InstanceUtils.createArray(List.of());
        for (NTree child : children) {
            arrayInstance.add(child.getFieldValue(fieldPath));
        }
        return arrayInstance;
    }

    @Override
    public void load() {
        children.forEach(NTree::load);
    }

}
