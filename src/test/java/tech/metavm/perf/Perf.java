package tech.metavm.perf;

import com.fasterxml.jackson.core.type.TypeReference;
import tech.metavm.common.Result;
import tech.metavm.flow.rest.FlowExecutionRequest;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.util.HttpUtils;
import tech.metavm.util.TestUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;

public class Perf {

    private static HttpClient client;
    private static FlowExecutionRequest request;

    public static final String jsonStr =
            "{\"flowId\":1044501450,\"instanceId\":1049900999,\"arguments\":[{\"type\":1,\"value\":1,\"primitiveKind\":1},{\"elementAsChild\":false,\"type\":3,\"elements\":[{\"type\":2,\"id\":1050001003,\"displayValue\":\"10\"},{\"type\":2,\"id\":1050001002,\"displayValue\":\"5\"},{\"type\":2,\"id\":1050001001,\"displayValue\":\"10\"}]}]}";

    public static void main(String[] args) throws URISyntaxException, InterruptedException {
        var uri = new URI("https://metavm.tech/api/flow/execute");
        client = HttpUtils.buildClient(uri);
        request = TestUtils.parseJson(jsonStr, FlowExecutionRequest.class);
        int numRuns = 1;
        int numThreads = 1;
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < numRuns; j++) {
                    try {
                        HttpUtils.post(client, uri, request, new TypeReference<Result<InstanceDTO>>() {
                        });
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            threads[i].start();
        }
        long start = System.currentTimeMillis();
        for (int i = 0; i < numThreads; i++)
            threads[i].join();
        long elapsed = System.currentTimeMillis() - start;
        System.out.printf("Total time: %d ms%n", elapsed);
        System.out.printf("Average time: %d ms%n", elapsed / numRuns);
    }

}
