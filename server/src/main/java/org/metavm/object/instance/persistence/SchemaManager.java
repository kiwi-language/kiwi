package org.metavm.object.instance.persistence;

import lombok.SneakyThrows;

public interface SchemaManager {

    void createInstanceTable(long appId, String table);

    void createIndexEntryTable(long appId, String table);

    void switchTable(long appId);

    @SneakyThrows
    void revert(long appId);

    void dropTmpTables(long appId);

    void dropAllTables(long appId);

}
