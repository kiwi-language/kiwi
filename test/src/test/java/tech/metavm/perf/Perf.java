package tech.metavm.perf;

import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.Result;
import tech.metavm.flow.rest.FlowExecutionRequest;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.util.HttpUtils;
import tech.metavm.util.TestUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;

public class Perf {

    public static final Logger LOGGER = LoggerFactory.getLogger(Perf.class);

    private static HttpClient client;
    private static FlowExecutionRequest request;

    public static final String jsonStr =
            "{\"flowId\":1008800179,\"instanceId\":1010400209,\"arguments\":[{\"type\":1,\"value\":1,\"primitiveKind\":1}]}";

    public static void main(String[] args) throws URISyntaxException, InterruptedException {
        var uri = new URI("https://metavm.tech/api/flow/execute");
        client = HttpUtils.buildClient(uri);
        request = TestUtils.parseJson(jsonStr, FlowExecutionRequest.class);
        int numRuns = 500;
        int numThreads = 20;
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < numRuns; j++) {
                    try {
                        HttpUtils.post(client, uri, request, new TypeReference<Result<InstanceDTO>>() {
                        });
                    } catch (Exception e) {
                        LOGGER.error("Request error", e);
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
        System.out.printf("Average RT: %d ms%n", elapsed / numRuns);
        System.out.printf("TPS: %d%n", numRuns * numThreads * 1000 / elapsed);
    }

}
