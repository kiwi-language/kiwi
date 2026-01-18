package org.manul.object.instance.persistence;

import lombok.SneakyThrows;

public interface SchemaManager {

    @SneakyThrows
    boolean instanceTableExists(long appId, String table);

    void createInstanceTable(long appId, String table);

    void createIndexEntryTable(long appId, String table);

    void switchTable(long appId, boolean backup);

    void revert(long appId);

    void dropTmpTables(long appId);

    void dropAllTables(long appId);

}
