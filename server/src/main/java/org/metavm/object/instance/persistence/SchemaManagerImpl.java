package org.metavm.object.instance.persistence;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.metavm.ddl.Commit;
import org.metavm.util.Utils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;

@Slf4j
@Component
public class SchemaManagerImpl implements SchemaManager {

    @Qualifier("secondaryDataSource")
    private final DataSource dataSource;

    public SchemaManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        Commit.dropTmpTableHook = this::dropTmpTables;
    }

    @SneakyThrows
    @Override
    public void createInstanceTable(long appId, String table) {
        try (var connection = dataSource.getConnection(); var stmt = connection.createStatement()) {
            var sql = "create table " + table + "_" + appId +
                    """
                    (
                        id           bigint                     not null
                        primary key,
                        app_id       bigint                     not null,
                        data         bytea                      not null,
                        version      bigint default '0'::bigint not null,
                        sync_version bigint default '0'::bigint not null,
                        deleted_at   bigint default '0'::bigint not null,
                        next_node_id bigint default 0           not null
                    )""";
            log.info("Creating table {}_{}", table, appId);
            stmt.executeUpdate(sql);
        }
    }

    @SneakyThrows
    @Override
    public void createIndexEntryTable(long appId, String table) {
        try (var connection = dataSource.getConnection(); var stmt = connection.createStatement()) {
            var sql = "create table " + table + "_" + appId +
                    """
                    (
                        app_id      bigint not null,
                        index_id    bytea  not null,
                        data        bytea  not null,
                        instance_id bytea  not null,
                        primary key (app_id, index_id, data, instance_id)
                    )""";
            log.info("SQL: {}", sql);
            stmt.executeUpdate(sql);
        }
    }

    //            stmt.executeUpdate(String.format("drop table instance_%d, index_entry_%d", appId, appId));
    @SneakyThrows
    @Override
    public void switchTable(long appId) {
        Utils.require(TransactionSynchronizationManager.isActualTransactionActive());
        var connection = DataSourceUtils.getConnection(dataSource);
        try (var stmt = connection.createStatement()) {
            log.info("Start switching table");
            stmt.addBatch(String.format("drop table instance_%d", appId));
            stmt.addBatch(String.format("drop table index_entry_%d", appId));
            stmt.addBatch(
                    String.format("alter table instance_tmp_%d rename to instance_%d", appId, appId)
            );
            stmt.addBatch(
                    String.format("alter table index_entry_tmp_%d rename to index_entry_%d", appId, appId)
            );
            stmt.executeBatch();
            log.info("Finished switching tables");
        }
    }

    @SneakyThrows
    private void switchTable0(long appId) {

    }

    @SneakyThrows
    public void dropTmpTables(long appId) {
        try (var connection = dataSource.getConnection(); var stmt = connection.createStatement()) {
            stmt.executeUpdate(String.format("drop table instance_tmp_%d, index_entry_tmp_%d", appId, appId));
        }
    }

    public static void main(String[] args) {

    }

}
