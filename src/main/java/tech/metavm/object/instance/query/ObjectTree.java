package tech.metavm.object.instance.query;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;

import java.util.*;

public class ObjectTree extends NTree {

    private final long instanceId;
    private final Map<String, NTree> fields = new LinkedHashMap<>();

    public ObjectTree(Path path, long instanceId) {
        super(path);
        this.instanceId = instanceId;
    }

    public long getInstanceId() {
        return instanceId;
    }

    public void setInstance(Instance instance) {
        Type type = instance.getType();
        for (Path childPath : path.getChildren()) {
            Field field = type.getFieldByName(childPath.getName());
            Object fieldValue = instance.getRaw(field);
            if(fieldValue == null) {
                continue;
            }
            if(field.isGeneralPrimitive()) {
                addField(new ValueTree(childPath, fieldValue));
            }
            else if(field.isSingleValued()) {
                addField(new ObjectTree(childPath, (Long) fieldValue));
            }
            else {
                addField(new ListTree(childPath, (List<Long>) fieldValue));
            }
        }
    }

    private void addField(NTree fieldValue) {
        fields.put(fieldValue.getName(), fieldValue);
    }

    @Override
    public List<ObjectTree> getChildObjectTrees() {
        List<ObjectTree> result = new ArrayList<>();
        for (NTree child : fields.values()) {
            if(child instanceof ObjectTree objectTree) {
                result.add(objectTree);
            }
            else if(child instanceof ListTree listTree) {
                result.addAll(listTree.getChildObjectTrees());
            }
        }
        return result;
    }

    public Map<String, Object> getValue() {
        Map<String, Object> map = new HashMap<>();
        fields.forEach((name, child) -> map.put(name, child.getValue()));
        return map;
    }

    public Object getFieldValue(String fieldPath) {
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
