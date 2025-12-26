package org.metavm.tools;

import org.apache.http.HttpHost;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.jsonk.Jsonk;
import org.metavm.object.instance.ColumnKind;
import org.metavm.util.Constants;
import org.metavm.util.Utils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.metavm.constant.FieldNames.*;

public class EsIndexCreator {

    public static final String HOST = "127.0.0.1";
    public static final int PORT = 9200;
    public static final String INDEX = "instance";

    private final RestClient client;

    public EsIndexCreator() {
        RestClientBuilder builder = RestClient.builder(new HttpHost(HOST, PORT));
        // Build the low-level client directly
        client = builder.build();
    }

    public Map<String, Object> buildSource() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put(APPLICATION_ID, Map.of("type", "long"));
        properties.put(TYPE, Map.of("type", "keyword"));
        properties.put(ID, Map.of("type", "long"));
        for (int level = 0; level < Constants.MAX_INHERITANCE_DEPTH; level++) {
            for (ColumnKind columnType : ColumnKind.values()) {
                for (int i = 0; i < columnType.count(); i++) {
                    if (columnType.esType() == null) {
                        continue;
                    }
                    String fieldName = "l" + level + "." + columnType.prefix() + i;
                    properties.put(fieldName, Map.of(
                            "type", columnType.esType()
                    ));
                }
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
        try {
            // 1. Create the Low-Level Request (PUT /instance)
            Request request = new Request("PUT", "/" + INDEX);

            // 2. Convert the Map source to a JSON String using your util
            String jsonSource = Jsonk.toJson(buildSource());
            request.setJsonEntity(jsonSource);

            // 3. Execute request
            Response response = client.performRequest(request);
            int statusCode = response.getStatusLine().getStatusCode();

            // 4. Handle success (200 OK)
            if (statusCode == 200) {
                System.out.printf("Create index response. index: %s, status: %d (Success)%n", INDEX, statusCode);
            } else {
                System.err.printf("Unexpected status code: %d%n", statusCode);
            }

        } catch (ResponseException e) {
            // Handle specific ES errors (e.g., 400 if index already exists)
            System.err.println("Elasticsearch Error: " + e.getMessage());
            throw new RuntimeException("Fail to create index (Status: " + e.getResponse().getStatusLine().getStatusCode() + ")", e);
        } catch (IOException e) {
            throw new RuntimeException("Fail to create index due to IO error", e);
        } finally {
            // Optional: Close client if this is a one-off tool execution
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        EsIndexCreator creator = new EsIndexCreator();
        // Print the JSON structure for debugging
        System.out.println("Source JSON: " + Jsonk.toJson(creator.buildSource()));
        // Run creation
        creator.create();
    }

}