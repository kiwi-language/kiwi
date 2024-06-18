package org.metavm.entity;

import org.metavm.object.instance.core.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MemoryStdIdStore implements StdIdStore {

    public static final Logger logger = LoggerFactory.getLogger(MemoryStdIdStore.class);
    private final Map<String, Id> ids = new HashMap<>();

    @Override
    public void save(Map<String, Id> ids) {
        this.ids.clear();
        this.ids.putAll(ids);
    }

    public Id get(String name) {
        return ids.get(name);
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

    public void log() {
        var buf = new StringBuilder("Standard IDs:\n");
        ids.forEach((name, id) -> buf.append(name).append(": ").append(id).append('\n'));
        logger.info(buf.toString());
    }

}
