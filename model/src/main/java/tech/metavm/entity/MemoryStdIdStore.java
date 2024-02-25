package tech.metavm.entity;

import java.util.HashMap;
import java.util.Map;

public class MemoryStdIdStore implements StdIdStore {

    private final Map<String, Long> ids = new HashMap<>();

    @Override
    public void save(Map<String, Long> ids) {
        this.ids.clear();
        this.ids.putAll(ids);
    }

    @Override
    public Map<String, Long> load() {
        return new HashMap<>(ids);
    }

    public MemoryStdIdStore copy() {
        var copy = new MemoryStdIdStore();
        copy.ids.putAll(ids);
        return copy;
    }

}
