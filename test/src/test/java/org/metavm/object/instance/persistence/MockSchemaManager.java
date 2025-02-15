package org.metavm.object.instance.persistence;

import lombok.extern.slf4j.Slf4j;
import org.metavm.ddl.Commit;

@Slf4j
public class MockSchemaManager implements SchemaManager  {

    private final MemMapperRegistry mapperRegistry;

    public MockSchemaManager(MemMapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
        Commit.dropTmpTableHook = this::dropTmpTables;
    }

    @Override
    public void createInstanceTable(long appId, String table) {
    }

    @Override
    public void createIndexEntryTable(long appId, String table) {

    }

    @Override
    public void switchTable(long appId) {
        log.info("Switching tables for application {}", appId);
        mapperRegistry.removeInstanceMapper(appId, "instance");
        mapperRegistry.removeIndexEntryMapper(appId, "index_entry");
        mapperRegistry.renameInstanceMapper(appId, "instance_tmp", "instance");
        mapperRegistry.renameIndexEntryMapper(appId, "index_entry_tmp", "index_entry");
    }

    private void dropTmpTables(long appId) {
        mapperRegistry.removeInstanceMapper(appId, "instance_tmp");
        mapperRegistry.removeIndexEntryMapper(appId, "index_entry_tmp");
    }

}
