package org.metavm.entity;

import org.metavm.object.instance.core.Id;

import java.util.HashMap;
import java.util.Map;

public class MemoryStdIdStore implements StdIdStore {

    private final Map<String, Id> ids = new HashMap<>();

    @Override
    public void save(Map<String, Id> ids) {
        this.ids.clear();
        this.ids.putAll(ids);
    }

    @Override
    public Map<String, Id> load() {
        return new HashMap<>(ids);
    }

    public MemoryStdIdStore copy() {
        var copy = new MemoryStdIdStore();
        copy.ids.putAll(ids);
        return copy;
    }

}
