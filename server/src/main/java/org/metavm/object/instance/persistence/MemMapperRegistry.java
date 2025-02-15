package org.metavm.object.instance.persistence;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.MemIndexEntryMapper;
import org.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import org.metavm.object.instance.persistence.mappers.InstanceMapper;
import org.metavm.object.instance.persistence.mappers.MemInstanceMapper;
import org.metavm.util.Utils;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Slf4j
public class MemMapperRegistry implements MapperRegistry {

    private final Map<TableKey, MemInstanceMapper> instanceMappers = new ConcurrentHashMap<>();
    private final Map<TableKey, MemIndexEntryMapper> indexEntryMappers = new ConcurrentHashMap<>();
    private final Set<String> tableNames = new ConcurrentSkipListSet<>();

    public MemMapperRegistry() {
    }

    public InstanceMapper getInstanceMapper(long appId, String table) {
        var key = new TableKey(appId, table);
        return instanceMappers.computeIfAbsent(key, t -> {
            tableNames.add(table + "_" + appId);
            return new MemInstanceMapper(table + "_" + appId);
        });
    }

    @Override
    public IndexEntryMapper getIndexEntryMapper(long appId, String table) {
        var key = new TableKey(appId, table);
        return indexEntryMappers.computeIfAbsent(key, t -> {
            tableNames.add(table + "_" + appId);
            return new MemIndexEntryMapper();
        });
    }

    public void renameInstanceMapper(long appId, String from, String to) {
        var key = new TableKey(appId, from);
        var mapper = Objects.requireNonNull(instanceMappers.remove(key), () -> "Instance mapper '" + from + "' not found");
        mapper.setTable(to + "_" + appId);
        instanceMappers.put(new TableKey(appId, to), mapper);
        tableNames.remove(from + "_" + appId);
        tableNames.add(to + "_" + appId);
    }

    public void renameIndexEntryMapper(long appId, String from, String to) {
        var key = new TableKey(appId, from);
        var mapper = Objects.requireNonNull(indexEntryMappers.remove(key), () -> "Index entry mapper '" + from + "' not found");
        indexEntryMappers.put(new TableKey(appId, to), mapper);
        tableNames.remove(from + "_" + appId);
        tableNames.add(to + "_" + appId);
    }

    public void removeInstanceMapper(long appId, String table) {
        instanceMappers.remove(new TableKey(appId, table));
        tableNames.remove(table + "_" + appId);
    }

    public void removeIndexEntryMapper(long appId, String table) {
        indexEntryMappers.remove(new TableKey(appId, table));
        tableNames.remove(table + "_" + appId);
    }

    public MemMapperRegistry copy() {
        var copy = new MemMapperRegistry();
        instanceMappers.forEach((t, mapper) -> copy.instanceMappers.put(t, mapper.copy()));
        indexEntryMappers.forEach((t, mapper) -> copy.indexEntryMappers.put(t, mapper.copy()));
        return copy;
    }

    private record TableKey(long appId, String name) {}

    public void logMappers() {
        log.trace("Mapper register @ {}", System.identityHashCode(this));
        log.trace("Instance mappers: {}", Utils.join(instanceMappers.keySet(), Objects::toString));
        log.trace("Index entry mappers: {}", Utils.join(indexEntryMappers.keySet(), Objects::toString));
    }

    public boolean tableExists(String name) {
        return tableNames.contains(name);
    }

}
