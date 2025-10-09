package org.metavm.object.instance.persistence;

import lombok.extern.slf4j.Slf4j;
import org.metavm.util.Hooks;

@Slf4j
public class MockSchemaManager implements SchemaManager  {

    private final MemMapperRegistry mapperRegistry;

    public MockSchemaManager(MemMapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    @Override
    public void createInstanceTable(long appId, String table) {
        mapperRegistry.createInstanceMapper(appId, table);
        Hooks.DROP_TABLES = this::dropAllTables;
    }

    @Override
    public void createIndexEntryTable(long appId, String table) {
        mapperRegistry.createIndexEntryMapper(appId, table);
    }

    @Override
    public void switchTable(long appId, boolean backup) {
        log.info("Switching tables for application {}", appId);
        if (backup) {
            mapperRegistry.renameInstanceMapper(appId, "instance", "instance_bak");
            mapperRegistry.renameIndexEntryMapper(appId, "index_entry", "index_entry_bak");
        }
        mapperRegistry.renameInstanceMapper(appId, "instance_tmp", "instance");
        mapperRegistry.renameIndexEntryMapper(appId, "index_entry_tmp", "index_entry");
    }

    @Override
    public void revert(long appId) {
        mapperRegistry.removeInstanceMapper(appId, "instance");
        mapperRegistry.removeIndexEntryMapper(appId, "index_entry");
        mapperRegistry.renameInstanceMapper(appId, "instance_bak", "instance");
        mapperRegistry.renameIndexEntryMapper(appId, "index_entry_bak", "index_entry");
    }

    @Override
    public void dropTmpTables(long appId) {
        mapperRegistry.tryRemoveInstanceMapper(appId, "instance_tmp");
        mapperRegistry.tryRemoveIndexEntryMapper(appId, "index_entry_tmp");
    }

    @Override
    public void dropAllTables(long appId) {
        mapperRegistry.removeInstanceMapper(appId, "instance");
        mapperRegistry.removeIndexEntryMapper(appId, "index_entry");
        mapperRegistry.tryRemoveInstanceMapper(appId, "instance_tmp");
        mapperRegistry.tryRemoveIndexEntryMapper(appId, "index_entry_tmp");
        mapperRegistry.tryRemoveInstanceMapper(appId, "instance_bak");
        mapperRegistry.tryRemoveIndexEntryMapper(appId, "index_entry_bak");
    }

}
