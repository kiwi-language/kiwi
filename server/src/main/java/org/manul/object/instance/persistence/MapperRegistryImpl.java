package org.manul.object.instance.persistence;

import org.manul.object.instance.persistence.mappers.IndexEntryMapper;
import org.manul.object.instance.persistence.mappers.IndexEntryMapperImpl;
import org.manul.object.instance.persistence.mappers.InstanceMapper;
import org.manul.object.instance.persistence.mappers.InstanceMapperImpl;
import org.manul.context.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component(module = "persistent")
public class MapperRegistryImpl implements MapperRegistry {

    private final DataSource dataSource;
    private final Map<Key, InstanceMapper> instanceMappers = new ConcurrentHashMap<>();
    private final Map<Key, IndexEntryMapper> indexEntryMappers = new ConcurrentHashMap<>();

    public MapperRegistryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public InstanceMapper getInstanceMapper(long appId, String table) {
        return instanceMappers.computeIfAbsent(
                new Key(appId, table),
                k -> new InstanceMapperImpl(dataSource, appId, table)
        );
    }

    @Override
    public IndexEntryMapper getIndexEntryMapper(long appId, String table) {
        return indexEntryMappers.computeIfAbsent(
                new Key(appId, table),
                k -> new IndexEntryMapperImpl(dataSource, appId, table)
        );
    }

    private record Key(long appId, String table) {}

}
