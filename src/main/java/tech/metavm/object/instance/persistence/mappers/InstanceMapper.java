package tech.metavm.object.instance.persistence.mappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.constant.ColumnNames;
import tech.metavm.object.instance.ColumnType;
import tech.metavm.object.instance.InsertSQLBuilder;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.InstanceTitlePO;
import tech.metavm.object.instance.persistence.VersionPO;
import tech.metavm.util.NncUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static tech.metavm.constant.ColumnNames.*;
import static tech.metavm.object.instance.ObjectTableConstant.TABLE_INSTANCE;

@Component
public class InstanceMapper {

    @Autowired
    private DataSource dataSource;
    
    private static final String SQL_SELECT_LAST_ID = "SELECT LAST_INSERT_ID()";

    private static final String INSERT_SQL;

    private static final String UPDATE_SQL;

    private static final String UPDATE_SYNC_VERSION_SQL =
            "UPDATE instance set sync_version = ? where id = ? and sync_version < ? and deleted_at = 0";

    static {
        InsertSQLBuilder sqlBuilder = new InsertSQLBuilder(TABLE_INSTANCE);
        sqlBuilder.addColumn(TITLE);
        sqlBuilder.addColumn(VERSION);
        ColumnType.sqlColumnNames().forEach(sqlBuilder::addColumn);
        INSERT_SQL = sqlBuilder.buildInsert();
        UPDATE_SQL = sqlBuilder.buildUpdate();
    }

    public int batchInsert(Collection<InstancePO> records) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement stmt = connection.prepareStatement(INSERT_SQL)
        ) {
            addUpdate(stmt, records, false);
            int affected = NncUtils.sum(stmt.executeBatch());

            if(affected > 0) {
                try (
                        PreparedStatement getLastIdStmt = connection.prepareStatement(SQL_SELECT_LAST_ID);
                        ResultSet lastIdRs = getLastIdStmt.executeQuery()
                ) {
                    if (!lastIdRs.next()) {
                        throw new RuntimeException("Fail to get last insert ID");
                    }
                    long id = lastIdRs.getLong(1) - affected + 1;
                    for (InstancePO record : records) {
                        record.setId(id++);
                    }
                }
            }
            return affected;
        }
        catch (SQLException e) {
            throw new RuntimeException("SQL Error", e);
        }
    }

    public int updateSyncVersion(List<VersionPO> versions) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement stmt = connection.prepareStatement(UPDATE_SYNC_VERSION_SQL)
        ) {
            for (VersionPO version : versions) {
                stmt.setLong(1, version.version());
                stmt.setLong(2, version.id());
                stmt.setLong(3, version.version());
                stmt.addBatch();
            }
            return NncUtils.sum(stmt.executeBatch());
        }
        catch (SQLException e) {
            throw new RuntimeException("SQL Error", e);
        }
    }

    public int batchUpdate(List<InstancePO> records) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement stmt = connection.prepareStatement(UPDATE_SQL)
        ) {
            addUpdate(stmt, records, true);
            return NncUtils.sum(stmt.executeBatch());
        }
        catch (SQLException e) {
            throw new RuntimeException("SQL Error", e);
        }
    }

    private void addUpdate(PreparedStatement stmt, Collection<InstancePO> records, boolean forUpdate) throws SQLException {
        for (InstancePO record : records) {
            int index = 1;
            stmt.setString(index++, record.title());
            stmt.setLong(index++, record.version());
            for (String column : ColumnType.sqlColumnNames()) {
                stmt.setObject(index++, record.get(column));
            }
            stmt.setLong(index++, record.tenantId());
            if(forUpdate) {
                stmt.setLong(index, record.id());
            }
            else {
                stmt.setLong(index, record.modelId());
            }
            stmt.addBatch();
        }
    }

    public int batchDelete(long tenantId, List<VersionPO> versions) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement stmt = connection.prepareStatement(batchDeleteSQL());
        ) {
            long now = System.currentTimeMillis();
            for (VersionPO version : versions) {
                int index = 1;
                stmt.setLong(index++, now);
                stmt.setLong(index++, version.version());
                stmt.setLong(index++, tenantId);
                stmt.setLong(index, version.id());
                stmt.addBatch();
            }
            return NncUtils.sum(stmt.executeBatch());
        }
        catch (SQLException e) {
            throw new RuntimeException("SQL Error", e);
        }
    }

    public List<InstancePO> selectByIds(long tenantId, List<Long> ids) {
        try(Connection connection = dataSource.getConnection();
            PreparedStatement stmt = connection.prepareStatement(selectByIdsSQL(ids.size()));
        ) {
            stmt.setLong(1, tenantId);
            int index = 2;
            for(Long id : ids) {
                stmt.setLong(index++, id);
            }
            return executeQuery(stmt);
        }
        catch (SQLException e) {
            throw new RuntimeException("SQL Error, tenantId: " + tenantId + ", objectId: " + ids, e);
        }
    }

    private List<InstancePO> executeQuery(PreparedStatement stmt) throws SQLException {
        try(ResultSet resultSet = stmt.executeQuery()) {
            List<InstancePO> records = new ArrayList<>();
            while(resultSet.next()) {
                InstancePO record = InstancePO.newInstance(
                        resultSet.getLong(ColumnNames.TENANT_ID),
                        resultSet.getLong(ID),
                        resultSet.getLong(ColumnNames.N_CLASS_ID),
                        resultSet.getString(TITLE),
                        resultSet.getLong(VERSION),
                        resultSet.getLong(SYNC_VERSION)
                );
                for (String column : ColumnType.sqlColumnNames()) {
                    record.put(column, resultSet.getObject(column));
                }
                records.add(record);
            }
            return records;
        }
    }

    private List<InstanceTitlePO> executeTitleQuery(PreparedStatement stmt) throws SQLException {
        try(ResultSet resultSet = stmt.executeQuery()) {
            List<InstanceTitlePO> titles = new ArrayList<>();
            while(resultSet.next()) {
                InstanceTitlePO title = new InstanceTitlePO(
                        resultSet.getLong(ID),
                        resultSet.getString(TITLE)
                );
                titles.add(title);
            }
            return titles;
        }
    }

    private long executeCount(PreparedStatement stmt) throws SQLException {
        try(ResultSet resultSet = stmt.executeQuery()) {
            if(!resultSet.next()) {
                throw new RuntimeException("Empty result set");
            }
            return resultSet.getLong(1);
        }
    }

    public long countByModelIds(long tenantId, List<Long> typeIds) {
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement stmt = connection.prepareStatement(countByModelIdsSQL(typeIds.size()));
        ) {
            stmt.setLong(1, tenantId);
            int index = 2;
            for(Long typeId : typeIds) {
                stmt.setLong(index++, typeId);
            }
            return executeCount(stmt);
        }
        catch (SQLException e) {
            throw new RuntimeException("SQL Error", e);
        }
    }

    public List<InstancePO> selectByModelIds(long tenantId, List<Long> modelIds, long start, long limit) {
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement stmt = connection.prepareStatement(selectByModelIdsSQL(modelIds.size()))
        ) {
            stmt.setLong(1, tenantId);
            int index = 2;
            for(Long id : modelIds) {
                stmt.setLong(index++, id);
            }
            stmt.setLong(index++, start);
            stmt.setLong(index, limit);
            return executeQuery(stmt);
        }
        catch (SQLException e) {
            throw new RuntimeException("SQL Error", e);
        }
    }

    public List<InstanceTitlePO> selectTitleByIds(long tenantId, List<Long> ids) {
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement stmt = connection.prepareStatement(selectTitleByIdsSQL(ids.size()))
        ) {
            stmt.setLong(1, tenantId);
            int index = 2;
            for(Long id : ids) {
                stmt.setLong(index++, id);
            }
            return executeTitleQuery(stmt);
        }
        catch (SQLException e) {
            throw new RuntimeException("SQL Error", e);
        }
    }

    private String countByModelIdsSQL(int numItems) {
        return InstanceSQLBuilder.count(true, numItems);
    }

    private String selectByIdsSQL(int numItems) {
        return InstanceSQLBuilder.select(false, numItems, false);
    }

    private String selectByModelIdsSQL(int numItems) {
        return InstanceSQLBuilder.select(true, numItems, true);
    }

    private String batchDeleteSQL() {
        return InstanceSQLBuilder.builder()
                .update(DELETED_AT, VERSION)
                .whereEq(ID)
                .build();
    }

    private String selectTitleByIdsSQL(int numItems) {
        return InstanceSQLBuilder.builder()
                .select(ID, TITLE)
                .whereIn(ID, numItems)
                .build();
    }

}
