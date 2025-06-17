package org.metavm.object.instance.persistence.mappers;

import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.metavm.object.instance.core.TreeVersion;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.object.instance.persistence.VersionPO;
import org.metavm.util.Utils;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class InstanceMapperImpl implements InstanceMapper {

    private final DataSource dataSource;

    public static final String columns = "id, app_id, data, version, sync_version, next_node_id";
    private final String selectByIdSQL;
    private final String batchSelectByIdsSQL;
    private final String insertSQL;
    private final String updateSQL;
    private final String upsertSQL;
    private final String deleteSQL;
    private final String tryDeleteSQL;
    private final String batchPhysicalDeleteSQL;
    private final String updateSyncVersionSQL;
    private final String scanTreesSQL;
    private final String selectVersionsSQL;
    private final String filterDeletedIdsSQL;
    private final String selectPhysicalSQL;

    public InstanceMapperImpl(DataSource dataSource, long table, String name) {
        var t = String.format("%s_%d", name, table);
        this.dataSource = dataSource;
        selectByIdSQL = String.format("select %s from %s where id = ? and deleted_at = 0", columns, t);
        selectPhysicalSQL = String.format("select %s from %s where id = ?", columns, t);
        batchSelectByIdsSQL = String.format(
                "select %s from %s where id = any(?) and app_id = ? and deleted_at = 0",
                columns, t
        );
        insertSQL = String.format("insert into %s (%s) values (?, ?, ?, ?, ?, ?)", t, columns);
        updateSQL = String.format(
                "update %s set data = ?, version = ?, sync_version = ?, next_node_id = ? " +
                        "where id = ? and app_id = ? and deleted_at = 0",
                t
        );
        upsertSQL = String.format(
                "insert into %s (%s) values (?, ?, ?, ?, ?, ?) " +
                        "on conflict(id)" +
                        "do update set data = EXCLUDED.data, version = EXCLUDED.version, " +
                        "sync_version = EXCLUDED.sync_version, next_node_id = EXCLUDED.next_node_id",
                t, columns
        );
        deleteSQL = String.format(
                "update %s set deleted_at = ? where deleted_at = 0 and id = ? and app_id = ?",
                t
        );
        tryDeleteSQL = String.format(
                """
                INSERT INTO %s (id, app_id, data, version, sync_version, next_node_id, deleted_at)
                VALUES (?, ?, E'\\x', 0, 0, 0, ?)
                ON CONFLICT (id) DO UPDATE
                  SET deleted_at = ?
                """,
                t
        );
        updateSyncVersionSQL = String.format(
                "update %s set sync_version = ? where id = ? and app_id = ? and deleted_at = 0 and sync_version < ?",
                t
        );
        scanTreesSQL = String.format(
                "select %s from %s where id > ? and app_id = ? and deleted_at = 0 order by id limit ?",
                columns, t
        );
        selectVersionsSQL = String.format(
                "select id, version from %s where id = any(?) and app_id = ? and deleted_at = 0",
                t
        );
        batchPhysicalDeleteSQL = String.format(
                "delete from %s where id = any(?)",
                t
        );
        filterDeletedIdsSQL = String.format(
                "select id from %s where id = any(?) and deleted_at != 0",
                t
        );
    }

    @SneakyThrows
    @Override
    public InstancePO selectById(long id) {
        var connection = getConnection();
        try {
            var stmt = connection.prepareStatement(selectByIdSQL);
            stmt.setLong(1, id);
            try(stmt; var rs = stmt.executeQuery()) {
                if (rs.next())
                    return readInstance(rs);
                else
                    return null;
            }
        } finally {
            returnConnection(connection);
        }
    }

    @SneakyThrows
    public InstancePO selectPhysicalById(long id) {
        var connection = getConnection();
        try {
            var stmt = connection.prepareStatement(selectPhysicalSQL);
            stmt.setLong(1, id);
            try(stmt; var rs = stmt.executeQuery()) {
                if (rs.next())
                    return readInstance(rs);
                else
                    return null;
            }
        } finally {
            returnConnection(connection);
        }
    }


    @SneakyThrows
    @Override
    public List<InstancePO> selectByIds(long appId, Collection<Long> ids) {
        if (ids.isEmpty()) return List.of();
        var connection = getConnection();
        try {
            var stmt = connection.prepareStatement(batchSelectByIdsSQL);
            stmt.setArray(1, connection.createArrayOf("bigint", ids.toArray()));
            stmt.setLong(2, appId);
            try (stmt; var rs = stmt.executeQuery()) {
                var results = new ArrayList<InstancePO>();
                while (rs.next()) {
                    results.add(readInstance(rs));
                }
                return results;
            }
        } finally {
            returnConnection(connection);
        }
    }

    @SneakyThrows
    @Override
    public List<Long> filterDeletedIds(Collection<Long> ids) {
        var connection = getConnection();
        try {
            var stmt = connection.prepareStatement(filterDeletedIdsSQL);
            stmt.setArray(1, connection.createArrayOf("bigint", ids.toArray()));
            try(stmt; var rs = stmt.executeQuery()) {
                var deletedIds = new ArrayList<Long>();
                while (rs.next())
                    deletedIds.add(rs.getLong(1));
                return deletedIds;
            }
        } finally {
            returnConnection(connection);
        }
    }

    @SneakyThrows
    @Override
    public void batchInsert(Collection<InstancePO> records) {
        var connection = getConnection();
        try (var ps = connection.prepareStatement(insertSQL)) {
            for (InstancePO record : records) {
                ps.setLong(1, record.getId());
                ps.setLong(2, record.getAppId());
                ps.setBytes(3, record.getData());
                ps.setLong(4, record.getVersion());
                ps.setLong(5, record.getSyncVersion());
                ps.setLong(6, record.getNextNodeId());
                ps.addBatch();
            }
            ps.executeBatch();
        } finally {
            returnConnection(connection);
        }
    }

    @SneakyThrows
    @Override
    public void batchUpdate(Collection<InstancePO> records) {
        var connection = getConnection();
        try (var ps = connection.prepareStatement(updateSQL)) {
            for (InstancePO record : records) {
                ps.setBytes(1, record.getData());
                ps.setLong(2, record.getVersion());
                ps.setLong(3, record.getSyncVersion());
                ps.setLong(4, record.getNextNodeId());
                ps.setLong(5, record.getId());
                ps.setLong(6, record.getAppId());
                ps.addBatch();
            }
            ps.executeBatch();
        } finally {
            returnConnection(connection);
        }
    }

    @SneakyThrows
    @Override
    public void batchUpsert(Collection<InstancePO> records) {
        var connection = getConnection();
        try (var ps = connection.prepareStatement(upsertSQL)) {
            for (InstancePO record : records) {
                ps.setLong(1, record.getId());
                ps.setLong(2, record.getAppId());
                ps.setBytes(3, record.getData());
                ps.setLong(4, record.getVersion());
                ps.setLong(5, record.getSyncVersion());
                ps.setLong(6, record.getNextNodeId());
                ps.addBatch();
            }
            ps.executeBatch();
        } finally {
            returnConnection(connection);
        }
    }

    @SneakyThrows
    @Override
    public void batchDelete(long appId, long timestamp, Collection<VersionPO> versions) {
        var connection = getConnection();
        try(var ps = connection.prepareStatement(deleteSQL)) {
            for (VersionPO version : versions) {
                ps.setLong(1, timestamp);
                ps.setLong(2, version.id());
                ps.setLong(3, version.appId());
                ps.addBatch();
            }
            ps.executeBatch();
        } finally {
            returnConnection(connection);
        }
    }

    @SneakyThrows
    @Override
    public void tryBatchDelete(long appId, long timestamp, Collection<VersionPO> versions) {
        batchDelete(appId, timestamp, versions);
        var connection = getConnection();
        try(var ps = connection.prepareStatement(tryDeleteSQL)) {
            for (VersionPO version : versions) {
                ps.setLong(1, version.id());
                ps.setLong(2, version.appId());
                ps.setLong(3, timestamp);
                ps.setLong(4, timestamp);
                ps.addBatch();
            }
            ps.executeBatch();
        } finally {
            returnConnection(connection);
        }

    }

    @SneakyThrows
    private void physicalDeleteByIds(List<Long> ids) {
        var connection = getConnection();
        try(var ps = connection.prepareStatement(batchPhysicalDeleteSQL)) {
            ps.setArray(1, connection.createArrayOf("bigint", ids.toArray()));
            ps.executeUpdate();
        } finally {
            returnConnection(connection);
        }
    }

    @SneakyThrows
    @Override
    public int updateSyncVersion(List<VersionPO> versions) {
        var connection = getConnection();
        try(var ps = connection.prepareStatement(updateSyncVersionSQL)) {
            for (VersionPO version : versions) {
                ps.setLong(1, version.version());
                ps.setLong(2, version.id());
                ps.setLong(3, version.appId());
                ps.setLong(4, version.version());
                ps.addBatch();
            }
            var counts = ps.executeBatch();
            var countSum = 0;
            for (int count : counts) {
                countSum += count;
            }
            return countSum;
        } finally {
            returnConnection(connection);
        }
    }

    @SneakyThrows
    @Override
    public List<Long> scanTrees(long appId, long startId, long limit) {
        var connection = getConnection();
        try (var ps = connection.prepareStatement(scanTreesSQL)) {
            ps.setLong(1, startId);
            ps.setLong(2, appId);
            ps.setLong(3, limit);
            var results = new ArrayList<Long>();
            var rs = ps.executeQuery();
            while (rs.next()) results.add(rs.getLong("id"));
            return results;
        } finally {
            returnConnection(connection);
        }
    }

    @SneakyThrows
    @Override
    public List<TreeVersion> selectVersions(long appId, List<Long> ids) {
        var connection = getConnection();
        try (var ps = connection.prepareStatement(selectVersionsSQL)) {
            ps.setArray(1, connection.createArrayOf("bigint", ids.toArray()));
            ps.setLong(2, appId);
            var results = new ArrayList<TreeVersion>();
            var rs = ps.executeQuery();
            while (rs.next())
                results.add(new TreeVersion(rs.getLong("id"), rs.getLong("version")));
            return results;
        } finally {
            returnConnection(connection);
        }
    }

    private InstancePO readInstance(ResultSet rs) throws SQLException {
        return new InstancePO(
                rs.getLong("app_id"),
                rs.getLong("id"),
                rs.getBytes("data"),
                rs.getLong("version"),
                rs.getLong("sync_version"),
                rs.getLong("next_node_id")
        );
    }

    private Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    private void returnConnection(Connection connection) throws SQLException {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            connection.close();
    }

    @SneakyThrows
    public static void main(String[] args) {

        var dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUsername("postgres");
        dataSource.setPassword("85263670");
        dataSource.setJdbcUrl("jdbc:postgresql://127.0.0.1:5432/object");
        dataSource.setMaximumPoolSize(1);
        dataSource.setMinimumIdle(1);

        var mapper = new InstanceMapperImpl(dataSource, 1, "instance");
        mapper.batchInsert(List.of(
                new InstancePO(
                        1,
                        1,
                        "foo".getBytes(StandardCharsets.UTF_8),
                        0,
                        0,
                        0
                )
        ));

        mapper.batchInsert(List.of(
                new InstancePO(
                        1,
                        2,
                        "bar".getBytes(StandardCharsets.UTF_8),
                        0,
                        0,
                        0
                )
        ));


        var instances = mapper.selectByIds(1, List.of(1L));
        Utils.require(instances.size() == 1);

        mapper.batchDelete(1, System.currentTimeMillis(), List.of(new VersionPO(1, 1, 1)));

        mapper.tryBatchDelete(1, System.currentTimeMillis(), List.of(new VersionPO(1, 2, 1)));
        Utils.require(mapper.selectPhysicalById(2) != null);
        Utils.require(mapper.selectById(2) == null);

        mapper.tryBatchDelete(1, System.currentTimeMillis(), List.of(new VersionPO(1, 3, 1)));
        Utils.require(mapper.selectById(3) == null);
        Utils.require(mapper.selectPhysicalById(3) != null);

        Utils.require(mapper.filterDeletedIds(List.of(1L, 2L, 3L, 4L)).equals(List.of(1L, 2L, 3L)));

        mapper.physicalDeleteByIds(List.of(1L, 2L, 3L));

    }
}
