package tech.metavm.object.instance.query;

import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ArrayInstance;
import tech.metavm.object.instance.PrimitiveInstance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;

import java.util.*;

public class ObjectTree extends NTree {

    private final ClassInstance instance;
    private final Map<String, NTree> fields = new LinkedHashMap<>();

    public ObjectTree(Path path, ClassInstance instance) {
        super(path);
        this.instance = instance;
    }

    public ClassInstance getInstance() {
        return instance;
    }

    public void load() {
        ClassType type = instance.getType();
        for (Path childPath : path.getChildren()) {
            Field field = type.getFieldByName(childPath.getName());
            Instance fieldValue = instance.get(field);
            if(fieldValue == null) {
                continue;
            }
            if(fieldValue instanceof ClassInstance inst) {
                addField(new ObjectTree(childPath, inst));
            }
            else if(fieldValue instanceof PrimitiveInstance primInst){
                addField(new ValueTree(childPath, primInst));
            }
            else if(fieldValue instanceof ArrayInstance array){
                addField(new ListTree(childPath, array.getElements()));
            }
        }
    }

    private void addField(NTree fieldValue) {
        fields.put(fieldValue.getName(), fieldValue);
    }

    @Override
    public List<NTree> getChildren() {
        List<NTree> result = new ArrayList<>();
        for (NTree child : fields.values()) {
            if(child instanceof ObjectTree objectTree) {
                result.add(objectTree);
            }
            else if(child instanceof ListTree listTree) {
                result.addAll(listTree.getChildren());
            }
        }
        return result;
    }

    public ClassInstance getValue() {
//        Map<String, Object> map = new HashMap<>();
//        fields.forEach((name, child) -> map.put(name, child.getValue()));
//        return map;
        return instance;
    }

    public Instance getFieldValue(String fieldPath) {
        int idx = fieldPath.indexOf('.');
        if(idx > 0) {
            String fieldName = fieldPath.substring(0, idx);
            String subPath = fieldPath.substring(idx + 1);
            NTree subTree = fields.get(fieldName);
            if(subTree instanceof ObjectTree objSubTree) {
                return objSubTree.getFieldValue(subPath);
            }
            if(subTree instanceof ListTree listSubTree) {
                return listSubTree.getFieldValue(subPath);
            }
            throw new RuntimeException("Can't get field '" + fieldPath + "' from tree: " + this);
        }
        else {
            return fields.get(fieldPath).getValue();
        }

    }

}
