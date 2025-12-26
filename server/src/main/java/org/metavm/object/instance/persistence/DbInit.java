package org.metavm.object.instance.persistence;

import lombok.extern.slf4j.Slf4j;
import org.metavm.context.Component;

import javax.sql.DataSource;
import java.sql.SQLException;

@Slf4j
@Component
public class DbInit {

    private final DataSource dataSource;

    public DbInit(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static final String DDL = """
            create table id_region
            (
                type_category integer not null
                    primary key,
                start_id      bigint  not null,
                end_id        bigint  not null,
                next_id       bigint  not null
            );
                        
            create table id_block
            (
                id        bigint   not null
                    primary key,
                app_id    bigint   not null,
                type_id   bigint   not null,
                start_id  bigint   not null,
                end_id    bigint   not null,
                next_id   bigint   not null,
                is_active boolean  not null,
                type_tag  smallint not null
            );
                       
            create index idx_end
                on id_block (end_id);
                        
            create table files
            (
                name    varchar(255) not null
                    primary key,
                content bytea        not null
            );
                        
            create table reference
            (
                id             bigserial
                    primary key,
                app_id         bigint  not null,
                kind           integer not null,
                source_tree_id bigint  not null,
                target_id      bytea   not null,
                field_id       bytea
            );
                        
            create index target_idx
                on reference (app_id, kind, target_id);
                        
            create table wal
            (
                id         bigserial
                    primary key,
                app_id     bigint              not null,
                data       bytea               not null,
                state      integer   default 0 not null,
                created_at timestamp default CURRENT_TIMESTAMP
            );
                        
            create table id_sequence
            (
                next_id bigint not null
            );
                        
            create table instance_log
            (
                id     bigserial
                    primary key,
                app_id bigint  not null,
                data   bytea   not null,
                status integer not null
            );
                        
            create table instance_2
            (
                id           bigint                     not null
                    primary key,
                app_id       bigint                     not null,
                data         bytea                      not null,
                version      bigint default '0'::bigint not null,
                sync_version bigint default '0'::bigint not null,
                deleted_at   bigint default '0'::bigint not null,
                next_node_id bigint default 0           not null
            );
                        
            create table index_entry_2
            (
                app_id      bigint not null,
                index_id    bytea  not null,
                data        bytea  not null,
                instance_id bytea  not null,
                primary key (app_id, index_id, data, instance_id)
            );
                        
            create table instance_1
            (
                id           bigint                     not null
                    primary key,
                app_id       bigint                     not null,
                data         bytea                      not null,
                version      bigint default '0'::bigint not null,
                sync_version bigint default '0'::bigint not null,
                deleted_at   bigint default '0'::bigint not null,
                next_node_id bigint default 0           not null
            );
                        
            create table index_entry_1
            (
                app_id      bigint not null,
                index_id    bytea  not null,
                data        bytea  not null,
                instance_id bytea  not null,
                primary key (app_id, index_id, data, instance_id)
            );
            """;

    public void run() {
        try (var conn = dataSource.getConnection()){
            conn.setAutoCommit(false);
            conn.createStatement().executeUpdate(DDL);
            conn.commit();
            log.info("Database initialized successfully");
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
