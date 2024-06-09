package tech.metavm;

import com.alibaba.druid.pool.DruidDataSource;
import tech.metavm.entity.DatabaseStdIdStore;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.Type;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

public class Lab {

    public static final String hex = "049EC9080C0B00180282C608002892020482C608021E0401A0C8081802031E6973536F7572636550526573656E74040B02161802FCC50800285400340324E69DA5E6BA90E698AFE590A6E5AD98E59CA85C04007C0C1802E2C90800289C0202E2C90802140A02D6CA08009E0292010B04161802F8C50800285E009E010C1802CEC60800285A02CEC60804040C191802FAC508002832020C18029CC60800288802000A0C1802E2C90800289C0202E2C90802140A02D6CA08009E02B4010B06161802E8C508002842020B081802E8C50800284202E8C5080818030CE8A786E59BBE2A0308766965773C0C18029CC60800288802009E010A029EC908009202B6010401BE010B0A151802FAC50800283200E6010400EA010A02CACB080056";

    private static Class<?> klass = Type.class;

    public static void main(String[] args) throws IOException, SQLException {
        try (DruidDataSource dataSource = new DruidDataSource()) {
            dataSource.setDriverClassName("org.postgresql.Driver");
            dataSource.setUrl("jdbc:postgresql://127.0.0.1:5432/object");
            dataSource.setUsername("postgres");
            dataSource.setPassword("85263670");
            dataSource.setMaxActive(1);

            try (Connection connection = dataSource.getConnection();) {
                var stmt = connection.createStatement();
                var rs = stmt.executeQuery("select content from files");
                rs.next();
                var input = rs.getBinaryStream(1);
                var bout = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int n;
                while ((n = input.read(buf)) != -1)
                    bout.write(buf, 0, n);
                var bytes = bout.toByteArray();
                var ids = DatabaseStdIdStore.buildIds(bytes);
                var newIds = new HashMap<String, Id>();
                ids.forEach((name, id) -> {
                    if(name.contains("isSource"))
                        System.out.printf("%s: %s%n", name, id);
                });
                System.out.println(ids.size());
            }
        }

    }


}
