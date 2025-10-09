package org.metavm.object.instance.persistence;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.metavm.common.ErrorCode;
import org.metavm.util.BusinessException;
import org.metavm.util.Hooks;
import org.metavm.util.Utils;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@Component
public class SchemaManagerImpl implements SchemaManager {

    private final DataSource dataSource;

    public SchemaManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        Hooks.DROP_TABLES = this::dropAllTables;
    }

    @SneakyThrows
    @Override
    public void createInstanceTable(long appId, String table) {
        var connection = getConnection();
        try (var stmt = connection.createStatement()) {
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
        } finally {
            returnConnection(connection);
        }
    }

    @SneakyThrows
    @Override
    public void createIndexEntryTable(long appId, String table) {
        var connection = getConnection();
        try (var stmt = connection.createStatement()) {
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
        } finally {
            returnConnection(connection);
        }
    }

    @SneakyThrows
    @Override
    public void switchTable(long appId, boolean backup) {
        Utils.require(TransactionSynchronizationManager.isActualTransactionActive());
        var connection = getConnection();
        try (var stmt = connection.createStatement()) {
            log.info("Start switching table");
            if (backup) {
                stmt.addBatch(String.format("drop table if exists instance_bak_%d", appId));
                stmt.addBatch(String.format("drop table if exists index_entry_bak_%d", appId));
                stmt.addBatch(
                        String.format("alter table instance_%d rename to instance_bak_%d", appId, appId)
                );
                stmt.addBatch(
                        String.format("alter table index_entry_%d rename to index_entry_bak_%d", appId, appId)
                );
            } else {
                stmt.addBatch(String.format("drop table instance_%d", appId));
                stmt.addBatch(String.format("drop table index_entry_%d", appId));
            }
            stmt.addBatch(
                    String.format("alter table instance_tmp_%d rename to instance_%d", appId, appId)
            );
            stmt.addBatch(
                    String.format("alter table index_entry_tmp_%d rename to index_entry_%d", appId, appId)
            );
            stmt.executeBatch();
            log.info("Finished switching tables");
        } finally {
            returnConnection(connection);
        }
    }

    @Override
    @SneakyThrows
    public void revert(long appId) {
        var connection = getConnection();
        try (var stmt = connection.createStatement()) {
            connection.setAutoCommit(false);
            log.info("Start rollback");
            var checkSql = String.format("select count(*) from information_schema.tables where table_name = 'instance_bak_%d'", appId);
            var rs = stmt.executeQuery(checkSql);
            if (!rs.next() || rs.getInt(1) == 0) {
                throw new BusinessException(
                        ErrorCode.REVERSION_FAILED,
                        "No backup instance table found for appId " + appId
                );
            }
            checkSql = String.format("select count(*) from information_schema.tables where table_name = 'index_entry_bak_%d'", appId);
            rs = stmt.executeQuery(checkSql);
            if (!rs.next() || rs.getInt(1) == 0) {
                throw new BusinessException(
                        ErrorCode.REVERSION_FAILED,
                        "No backup index entry table found for appId " + appId
                );
            }
            stmt.addBatch(String.format("drop table if exists instance_%d", appId));
            stmt.addBatch(String.format("drop table if exists index_entry_%d", appId));
            stmt.addBatch(
                    String.format("alter table instance_bak_%d rename to instance_%d", appId, appId)
            );
            stmt.addBatch(
                    String.format("alter table index_entry_bak_%d rename to index_entry_%d", appId, appId)
            );
            stmt.executeBatch();
            connection.commit();
            log.info("Finished rollback");
        } catch (SQLException e) {
            log.error("Error during rollback", e);
        } finally {
            returnConnection(connection);
        }

    }

    @SneakyThrows
    @Override
    public void dropTmpTables(long appId) {
        var connection = getConnection();
        try (var stmt = connection.createStatement()) {
            stmt.executeUpdate(String.format("drop table if exists instance_tmp_%d, index_entry_tmp_%d", appId, appId));
        }
        finally {
            returnConnection(connection);
        }
    }

    @SneakyThrows
    @Override
    public void dropAllTables(long appId) {
        var connection = getConnection();
        try (var stmt = connection.createStatement())  {
            connection.setAutoCommit(false);
            stmt.addBatch(String.format("drop table if exists instance_%d", appId));
            stmt.addBatch(String.format("drop table if exists instance_tmp_%d", appId));
            stmt.addBatch(String.format("drop table if exists instance_bak_%d", appId));
            stmt.addBatch(String.format("drop table if exists index_entry_%d", appId));
            stmt.addBatch(String.format("drop table if exists index_entry_tmp_%d", appId));
            stmt.addBatch(String.format("drop table if exists index_entry_bak_%d", appId));
            stmt.executeBatch();
        } finally {
            returnConnection(connection);
        }
    }

    private Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    private void returnConnection(Connection connection) throws SQLException {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            connection.close();
    }
}
