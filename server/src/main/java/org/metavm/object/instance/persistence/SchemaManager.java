package org.metavm.object.instance.persistence;

public interface SchemaManager {

    void createInstanceTable(long appId, String table);

    void createIndexEntryTable(long appId, String table);

    void switchTable(long appId, boolean backup);

    void revert(long appId);

    void dropTmpTables(long appId);

    void dropAllTables(long appId);

}
