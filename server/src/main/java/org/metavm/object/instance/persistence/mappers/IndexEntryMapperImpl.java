package org.metavm.object.instance.persistence.mappers;

import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.instance.persistence.IndexEntryPO;
import org.metavm.object.instance.persistence.IndexKeyPO;
import org.metavm.object.instance.persistence.IndexQueryPO;
import org.metavm.util.Utils;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class IndexEntryMapperImpl implements IndexEntryMapper {

    public static final String columns = "app_id, index_id, data, instance_id";

    private final DataSource dataSource;
    private final long appId;
    private final String[] countSQLs = new String[4];
    private final String[] querySQLs = new String[4];
    private final String countRangeSQL;
    private final String scanSQL;
    private final String insertSQL;
    private final String tryInsertSQL;
    private final String deleteSQL;
    private final String table;

    public IndexEntryMapperImpl(DataSource dataSource, long appId, String table) {
        this.appId = appId;
        this.dataSource = dataSource;
        var t = String.format("%s_%d", table, appId);
        this.table = t;
        for (int i = 0; i < 4; i++) {
            countSQLs[i] = buildQuerySQL("count(1)", t, (i & 1) != 0, (i & 2) != 0);
        }
        for (int i = 0; i < 4; i++) {
            querySQLs[i] = buildQuerySQL(columns, t, (i & 1) != 0, (i & 2) != 0);
        }
        countRangeSQL = String.format(
                "select count(1) from %s where app_id = ? and index_id = ? and data between ? and ?",
                t
        );
        scanSQL = String.format(
                "select %s from %s where app_id = ? and index_id = ? and data between ? and ?",
                columns, t
        );
        insertSQL = String.format("insert into %s (%s) values (?, ?, ?, ?)", t, columns);
        tryInsertSQL = String.format(
                "insert into %s (%s) values (?, ?, ? ,?) on conflict(app_id, index_id, data, instance_id) do nothing",
                t, columns
        );
        deleteSQL = String.format(
                "delete from %s where app_id = ? and index_id = ? and data = ? and instance_id = ?",
                t
        );
    }

    private String buildQuerySQL(String select, String table, boolean withLowerBound, boolean withUpperBound) {
        var sb = new StringBuilder("select ").append(select).append(" from ").append(table)
                .append(" where app_id = ? and index_id = ?");
        if (withLowerBound)
            sb.append(" and data >= ?");
        if (withUpperBound)
            sb.append(" and data <= ?");
        return sb.toString();
    }

    @SneakyThrows
    @Override
    public long count(IndexQueryPO queryPO) {
        var connection = getConnection();
        try {
            var ps = getQueryPS(connection, countSQLs, queryPO);
            var rs = ps.executeQuery();
            Utils.require(rs.next());
            return rs.getLong(1);
        }
        finally {
            returnConnection(connection);
        }
    }

    @SneakyThrows
    private PreparedStatement getQueryPS(Connection connection, String[] SQLs, IndexQueryPO query) {
        var appId = query.appId();
        var indexIdx = query.indexId();
        var from = query.from();
        var to = query.to();
        PreparedStatement ps;
        if (from == null && to == null) {
            ps = connection.prepareStatement(SQLs[0]);
            ps.setLong(1, appId);
            ps.setBytes(2, indexIdx);
        }
        else if (to == null) {
            ps = connection.prepareStatement(SQLs[1]);
            ps.setLong(1, appId);
            ps.setBytes(2, indexIdx);
            ps.setBytes(3, from.getData());
        }
        else if (from == null) {
            ps = connection.prepareStatement(SQLs[2]);
            ps.setLong(1, appId);
            ps.setBytes(2, indexIdx);
            ps.setBytes(3, to.getData());
        }
        else {
            ps = connection.prepareStatement(SQLs[3]);
            ps.setLong(1, appId);
            ps.setBytes(2, indexIdx);
            ps.setBytes(3, from.getData());
            ps.setBytes(4, to.getData());
        }
        return ps;
    }

    @SneakyThrows
    @Override
    public List<IndexEntryPO> query(IndexQueryPO queryPO) {
        var connection = getConnection();
        try {
            var ps = getQueryPS(connection, querySQLs, queryPO);
            return readIndexEntries(ps.executeQuery());
        }
        finally {
            returnConnection(connection);
        }
    }

    @SneakyThrows
    @Override
    public long countRange(long appId, IndexKeyPO from, IndexKeyPO to) {
        var connection = getConnection();
        try {
            var ps = connection.prepareStatement(countRangeSQL);
            ps.setLong(1, appId);
            ps.setBytes(2, from.getIndexId());
            ps.setBytes(3, from.getData());
            ps.setBytes(4, to.getData());
            return ps.executeQuery().getLong(1);
        }
        finally {
            returnConnection(connection);
        }
    }

    @SneakyThrows
    @Override
    public List<IndexEntryPO> scan(long appId, IndexKeyPO from, IndexKeyPO to) {
        var connection = getConnection();
        try {
            var ps = connection.prepareStatement(scanSQL);
            ps.setLong(1, appId);
            ps.setBytes(2, from.getIndexId());
            ps.setBytes(3, from.getData());
            ps.setBytes(4, to.getData());
            return readIndexEntries(ps.executeQuery());
        }
        finally {
            returnConnection(connection);
        }
    }

    @SneakyThrows
    @Override
    public List<IndexEntryPO> selectByInstanceIds(long appId, Collection<byte[]> instanceIds) {
        if (instanceIds.isEmpty()) return List.of();
        var sql = "select " + columns + " from " + table + " where app_id = ? and instance_id in (" +
                Utils.repeatWithDelimiter("?", instanceIds.size(), ",") + ")";
        var connection = getConnection();
        try {
            var ps =  connection.prepareStatement(sql);
            ps.setLong(1, appId);
            int i = 2;
            for (byte[] instanceId : instanceIds) {
                ps.setBytes(i++, instanceId);
            }
            return readIndexEntries(ps.executeQuery());
        }
        finally {
            returnConnection(connection);
        }
    }

    @SneakyThrows
    @Override
    public List<IndexEntryPO> selectByKeys(long appId, Collection<IndexKeyPO> keys) {
        if (keys.isEmpty()) return List.of();
        var sql = "select " + columns + " from " + table + " where app_id = ? and (index_id, data) in (" +
                Utils.repeatWithDelimiter("(?, ?)", keys.size(), ",") + ")";
        var connection = getConnection();
        try {
            var ps =  connection.prepareStatement(sql);
            ps.setLong(1, appId);
            int i = 2;
            for (IndexKeyPO key : keys) {
                ps.setBytes(i++, key.getIndexId() );
                ps.setBytes(i++, key.getData());
            }
            return readIndexEntries(ps.executeQuery());
        }
        finally {
            returnConnection(connection);
        }
    }

    @SneakyThrows
    @Override
    public void batchInsert(Collection<IndexEntryPO> items) {
        //noinspection DuplicatedCode
        var connection = getConnection();
        try {
            var ps = connection.prepareStatement(insertSQL);
            for (IndexEntryPO item : items) {
                ps.setLong(1, item.getAppId());
                ps.setBytes(2, item.getIndexId());
                ps.setBytes(3, item.getData());
                ps.setBytes(4, item.getInstanceId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        finally {
            returnConnection(connection);
        }
    }

    @SneakyThrows
    @Override
    public void tryBatchInsert(Collection<IndexEntryPO> items) {
        //noinspection DuplicatedCode
        var connection = getConnection();
        try {
            var ps = connection.prepareStatement(tryInsertSQL);
            for (IndexEntryPO item : items) {
                ps.setLong(1, item.getAppId());
                ps.setBytes(2, item.getIndexId());
                ps.setBytes(3, item.getData());
                ps.setBytes(4, item.getInstanceId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        finally {
            returnConnection(connection);
        }
    }

    @SneakyThrows
    @Override
    public void batchDelete(Collection<IndexEntryPO> items) {
        //noinspection DuplicatedCode
        var connection = getConnection();
        try {
            var ps = connection.prepareStatement(deleteSQL);
            for (IndexEntryPO item : items) {
                ps.setLong(1, item.getAppId());
                ps.setBytes(2, item.getIndexId());
                ps.setBytes(3, item.getData());
                ps.setBytes(4, item.getInstanceId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        finally {
            returnConnection(connection);
        }
    }

    @SneakyThrows
    private List<IndexEntryPO> readIndexEntries(ResultSet rs) {
        var result = new ArrayList<IndexEntryPO>();
        while (rs.next()) result.add(readIndexEntry(rs));
        return result;
    }

    @SneakyThrows
    private IndexEntryPO readIndexEntry(ResultSet rs) {
        return new IndexEntryPO(
                rs.getLong("app_id"),
                new IndexKeyPO(
                        rs.getBytes("index_id"),
                        rs.getBytes("data")
                ),
                rs.getBytes("instance_id")
        );
    }

    private Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    @SneakyThrows
    private void returnConnection(Connection connection) {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            connection.close();
    }


    public static void main(String[] args) {
        var dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUsername("postgres");
        dataSource.setPassword("85263670");
        dataSource.setJdbcUrl("jdbc:postgresql://127.0.0.1:5432/object");
        dataSource.setMaximumPoolSize(1);
        dataSource.setMinimumIdle(1);

        var appId = 2;
        var indexId = PhysicalId.of(1, 1).toBytes();
        var key = "foo".getBytes(StandardCharsets.UTF_8);
        var instanceId =  PhysicalId.of(2, 1).toBytes();

        var mapper = new IndexEntryMapperImpl(dataSource, appId, "index_entry");

        mapper.batchDelete(List.of(
                new IndexEntryPO(appId, new IndexKeyPO(indexId, key), instanceId)
        ));

        mapper.batchInsert(
                List.of(
                        new IndexEntryPO(appId, new IndexKeyPO(indexId, key), instanceId)
                )
        );

        mapper.tryBatchInsert(
                List.of(
                        new IndexEntryPO(
                                2L,
                                new IndexKeyPO(indexId, key),
                                instanceId
                        )
                )
        );

        var entries = mapper.selectByKeys(2L, List.of(
                new IndexKeyPO(indexId, key)
        ));
        Utils.require(entries.size() == 1);

        var entries1 = mapper.selectByInstanceIds(2, List.of(PhysicalId.of(2, 1).toBytes()));
        Utils.require(entries1.size() == 1);

        var entries2 = mapper.query(new IndexQueryPO(
                appId,
                indexId,
                new IndexKeyPO(indexId, key), new IndexKeyPO(indexId, key), false, 1L, 0
        ));
        Utils.require(entries2.size() == 1);

        mapper.batchDelete(List.of(
                new IndexEntryPO(
                        2L,
                        new IndexKeyPO(
                                PhysicalId.of(1, 1).toBytes(),
                                "foo".getBytes(StandardCharsets.UTF_8)
                        ),
                        PhysicalId.of(2, 1).toBytes()
                )
        ));

    }

}
