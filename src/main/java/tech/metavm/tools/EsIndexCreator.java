package tech.metavm.tools;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import tech.metavm.constant.FieldNames;
import tech.metavm.object.instance.ColumnType;
import tech.metavm.util.NncUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static tech.metavm.constant.FieldNames.TENANT_ID;
import static tech.metavm.constant.FieldNames.TYPE_ID;

public class EsIndexCreator {

    public static final String HOST = "127.0.0.1";
    public static final int PORT = 9200;
    public static final String INDEX = "instance";

    private final RestHighLevelClient client;

    public EsIndexCreator() {
        RestClientBuilder builder = RestClient.builder(new HttpHost(HOST, PORT));
        client = new RestHighLevelClient(builder);
    }

    public Map<String, Object> buildSource() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put(TENANT_ID, Map.of("type", "long"));
        properties.put(TYPE_ID, Map.of("type", "long"));
        for (ColumnType columnType : ColumnType.values()) {
            for(int i = 0; i < columnType.count(); i++) {
                String fieldName = columnType.prefix() + i;
                properties.put(fieldName, Map.of(
                        "type", columnType.esType()
                ));
            }
        }
        return Map.of(
                "settings", Map.of(
                        "number_of_shards", 3,
                        "number_of_replicas", 0
                ),
                "mappings", Map.of("properties", properties)
        );

    }

    public void create() {
        CreateIndexRequest request = new CreateIndexRequest(INDEX);
        request.source(buildSource());
        try {
            CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
            System.out.printf("Create index response. index: %s, ack: %s%n", response.index(), response.isAcknowledged());
        } catch (IOException e) {
            throw new RuntimeException("Fail to create index", e);
        }
    }

    public static void main(String[] args) {
        EsIndexCreator creator = new EsIndexCreator();
        System.out.println(NncUtils.toJSONString(creator.buildSource()));
    }

}
