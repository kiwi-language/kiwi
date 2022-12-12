package tech.metavm.tools;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import tech.metavm.util.Constants;
import tech.metavm.util.InternalException;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static tech.metavm.util.NncUtils.requireNonNull;

public class DiskFormatter {

    private static void clearDataBases() {
        try(DruidDataSource dataSource = new DruidDataSource()) {
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            dataSource.setUsername("root");
            dataSource.setPassword("85263670");
            dataSource.setMaxActive(1);
            dataSource.setUrl("jdbc:mysql://localhost:3306/object?allowMultiQueries=true");

            try(Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
                statement.execute("delete from id_block where id > 0");
                statement.execute("delete from tenant where id>0");
                statement.execute("delete from instance where id>=0");
                statement.execute("delete from instance_array where id>=0");
                statement.execute("delete from index_item where instance_id>=0");
                statement.execute("delete from relation where relation.src_instance_id>=0");
            }
            catch (SQLException e) {
                throw new InternalException("SQL Error", e);
            }
        }
    }

    private static void clearEs() {
        RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200));
        try(RestHighLevelClient client = new RestHighLevelClient(builder)) {
            DeleteByQueryRequest request = new DeleteByQueryRequest("instance");
            request.setQuery(new MatchAllQueryBuilder());
            BulkByScrollResponse response = client.deleteByQuery(request, RequestOptions.DEFAULT);
            System.out.println("Deleted documents: " + response.getCreated());
        } catch (IOException e) {
            throw new InternalException("Elasticsearch error", e);
        }
    }

    private static void deleteIdFiles() {
        String idDirPath = Constants.RESOURCE_CP_ROOT + "/id";
        File idDir = new File(idDirPath);
        for (String idFile : requireNonNull(idDir.list())) {
            if(!new File(idDirPath+ "/" + idFile).delete()) {
                System.err.println("Fail to delete id file '" + idFile + "'");
            }
        }
    }

    public static void main(String[] args) {
        clearEs();
        deleteIdFiles();
        clearDataBases();
    }

}
