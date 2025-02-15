package org.metavm;

import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.metavm.object.instance.core.Id;
import org.metavm.util.EncodingUtils;
import org.metavm.util.StreamVisitor;
import org.metavm.util.Utils;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;

public class Lab implements Serializable {

    public static final String hex =
            "0202008e86d7b907020d018e86d7b90700a88bcbbea2650a01fa85d7b9070000000000000000000000000000021c3031666338356437623930373236000007000001";

    public static final String hex2 =
            "0202028e86d7b907020d018e86d7b90700a88bcbbea2650a01fa85d7b9070000000000000000000000000000021c3031666338356437623930373236000001010000";

    public static long appId = 1000006000L;
    public static String commitId = "018e86d7b90700";

    public static void main(String[] args) throws IOException {
//        debugCommit(appId, commitId);
        debugCommit(EncodingUtils.hexToBytes(hex));
    }

    @SneakyThrows
    private static void debugCommit(long appId, String commitId) {
        var id = Id.parse(commitId);
        var treeId = id.getTreeId();
        var dataSource = getDataSource();
        try (var conn = dataSource.getConnection()) {
            var rs = conn.createStatement().executeQuery("select data from instance_" + appId + " where id = " + treeId);
            Utils.require(rs.next());
            var data = rs.getBytes(1);

            System.out.println(EncodingUtils.bytesToHex(data));
            debugCommit(data);
        }
    }


    private static void debugCommit(byte[] data) {

        var ref = new Object() {
            Boolean running;
        };
        new StreamVisitor(new ByteArrayInputStream(data)) {

            @Override
            public void visitBoolean() {
                var b = readBoolean();
                if (ref.running == null) {
                    ref.running = b;
                }
            }
        }.visitGrove();
        System.out.println("Running " + ref.running);

    }

    private static DataSource getDataSource() {
        var dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUsername("postgres");
        dataSource.setPassword("85263670");
        dataSource.setJdbcUrl("jdbc:postgresql://127.0.0.1:5432/object");
        dataSource.setMaximumPoolSize(1);
        dataSource.setMinimumIdle(1);
        return dataSource;
    }


}
