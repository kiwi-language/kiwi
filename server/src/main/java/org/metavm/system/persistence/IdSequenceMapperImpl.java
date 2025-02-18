package org.metavm.system.persistence;

import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.metavm.util.Utils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class IdSequenceMapperImpl implements IdSequenceMapper {

    private final DataSource dataSource;

    public IdSequenceMapperImpl(@Qualifier("secondaryDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @SneakyThrows
    @Override
    public Long selectNextId() {
        try (var connection = dataSource.getConnection(); var stmt = connection.createStatement()) {
            var rs = stmt.executeQuery("select next_id from id_sequence");
            return rs.next() ? rs.getLong(1) : null;
        }
    }

    @SneakyThrows
    @Override
    public void incrementNextId(long increment) {
        try (var connection = dataSource.getConnection();
             var stmt = connection.prepareStatement("update id_sequence set next_id = next_id + ?")) {
            stmt.setLong(1, increment);
            stmt.executeUpdate();
        }
    }

    @SneakyThrows
    @Override
    public void insert(long initial) {
        try (var connection = dataSource.getConnection();
             var stmt = connection.prepareStatement("insert into id_sequence (next_id) values (?)")) {
            stmt.setLong(1, initial);
            stmt.executeUpdate();
        }
    }

    @SneakyThrows
    private void delete() {
        try (var connection = dataSource.getConnection();
             var stmt = connection.prepareStatement("delete from id_sequence")) {
            stmt.executeUpdate();
        }
    }

    public static void main(String[] args) {
        var dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUsername("postgres");
        dataSource.setPassword("85263670");
        dataSource.setJdbcUrl("jdbc:postgresql://127.0.0.1:5432/object");
        dataSource.setMaximumPoolSize(1);
        dataSource.setMinimumIdle(1);

        var mapper = new IdSequenceMapperImpl(dataSource);
        Utils.require(mapper.selectNextId() == null);
        mapper.insert(100);
        Utils.require(mapper.selectNextId() == 100L);
        mapper.incrementNextId(10);
        Utils.require(mapper.selectNextId() == 110L);
        mapper.delete();
    }

}
