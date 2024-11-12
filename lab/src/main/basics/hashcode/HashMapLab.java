package hashcode;

import org.metavm.api.Component;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Component
public class HashMapLab {

    private final Map<Object, Object> map = new HashMap<>();

    public void put(Object key, Object value) {
        map.put(key, value);
    }

    public @Nullable Object get(Object key) {
        return map.get(key);
    }

    public void bazPut(String name, Object extra, Object value) {
        map.put(new HashCodeBaz(name, extra), value);
    }

    public @Nullable Object bazGet(String name, Object extra) {
        return map.get(new HashCodeBaz(name, extra));
    }

    public void listPut(List<Object> elements, Object value) {
        map.put(elements, value);
    }

    public @Nullable Object listGet(List<Object> elements) {
        return map.get(elements);
    }

    public void setPut(List<Object> elements, Object value) {
        map.put(new HashSet<>(elements), value);
    }

    public Object setGet(List<Object> elements) {
        return map.get(new HashSet<>(elements));
    }

    public void mapPut(List<MapEntry> entries, Object value) {
        map.put(createMap(entries), value);
    }

    public Object mapGet(List<MapEntry> entries) {
        return map.get(createMap(entries));
    }

//    private Map<Object, Object> createMap(List<MapEntry> entries) {
//        var m = new HashMap<>();
//        for (MapEntry entry : entries) {
//            m.put(entry.key(), entry.value());
//        }
//        return m;
//    }


    private Map<Object, Object> createMap(List<MapEntry> entries) {
        var m = new HashMap<>();
        int i = 0;
        var list = entries;
        while (i < list.size()) {
            var entry = list.get(i++);
            m.put(entry.key(), entry.value());
        }
        return m;
    }
}
