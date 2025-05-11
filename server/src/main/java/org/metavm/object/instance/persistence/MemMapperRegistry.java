package org.metavm.object.instance.persistence;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.MemIndexEntryMapper;
import org.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import org.metavm.object.instance.persistence.mappers.InstanceMapper;
import org.metavm.object.instance.persistence.mappers.MemInstanceMapper;
import org.metavm.util.InternalException;
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
        createInstanceMapper(1, "instance");
        createInstanceMapper(2, "instance");
        createIndexEntryMapper(1, "index_entry");
        createIndexEntryMapper(2, "index_entry");
    }

    public void createTables(long appId) {
        createInstanceMapper(appId, "instance");
        createIndexEntryMapper(appId, "index_entry");
    }

    public void createInstanceMapper(long appId, String table) {
        var tableName = table +  "_" + appId;
        if (!tableNames.add(tableName))
            throw new InternalException("Table " + tableName + " already exists");
        instanceMappers.put(new TableKey(appId, table), new MemInstanceMapper(tableName));
    }

    public void createIndexEntryMapper(long appId, String table) {
        var tableName = table + "_" + appId;
        if (!tableNames.add(tableName))
            throw new InternalException("Table " + tableName + " already exists");
        indexEntryMappers.put(new TableKey(appId, table), new MemIndexEntryMapper());
    }

    public InstanceMapper getInstanceMapper(long appId, String table) {
        var key = new TableKey(appId, table);
        return Objects.requireNonNull(instanceMappers.get(key), () -> "Cannot find table instance_" + appId);
    }

    @Override
    public IndexEntryMapper getIndexEntryMapper(long appId, String table) {
        var key = new TableKey(appId, table);
        return Objects.requireNonNull(indexEntryMappers.get(key),
                () -> "Cannot find table " + table + "_" + appId);
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
        var tableName = table + "_" + appId;
        if (!tableNames.remove(tableName))
            throw new InternalException("Table " + tableName + " doesn't exist");
        instanceMappers.remove(new TableKey(appId, table));
    }

    public void removeIndexEntryMapper(long appId, String table) {
        var tableName = table + "_" + appId;
        if (!tableNames.remove(tableName))
            throw new InternalException("Table " + tableName + " doesn't exist");
        indexEntryMappers.remove(new TableKey(appId, table));
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
