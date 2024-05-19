package tech.metavm.object.type;

import java.util.HashMap;
import java.util.Map;

public class MemTypeTagStore implements TypeTagStore {

    protected final Map<String, Integer> map = new HashMap<>();
    protected int nextTypeTag = 4;

    public int getTypeTag(String className) {
        return map.computeIfAbsent(className, k -> nextTypeTag++);
    }

    @Override
    public void save() {
    }

    public MemTypeTagStore copy() {
        var result = new MemTypeTagStore();
        result.nextTypeTag = nextTypeTag;
        result.map.putAll(map);
        return result;
    }
}
