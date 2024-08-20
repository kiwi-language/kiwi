package org.metavm.perf;

import org.metavm.util.Headers;
import org.metavm.util.NncUtils;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ApiPerf {

    public static final int THREAD_COUNT = 5;
    public static final int EXECUTIONS = 200;
    private static final String host = "http://localhost:8080/api";
    public static final long appId = 1000001021L;
    public static final String defaultProductKindId = "01dab8d6b90700";
    public static final String yuanCurrencyId = "01d4b8d6b90700";

//    private static final String host = "https://metavm.tech/api";
//    public static final long appId = 1000000019;
//    public static final String defaultProductKindId = "01dea8d6b90700";
//    public static final String yuanCurrencyId = "01d8a8d6b90700";

    private static final HttpClient client;
    public static final AtomicInteger failureCounter = new AtomicInteger();

    static {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
                .cookieHandler(CookieHandler.getDefault())
                .build();
    }

    public static void main(String[] args) {
        var threads = new ArrayList<Thread>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads.add(new Thread(ApiPerf::run));
        }
        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        var elapsed = System.currentTimeMillis() - start;
        System.out.println("Elapsed: " + elapsed + ", failures: " + failureCounter.get());
    }

    private static void run() {
        for (int i = 0; i < EXECUTIONS; i++) {
            request("/org/metavm/mlab/Product", Map.of(
                    "name", "Shoes",
                    "kind", defaultProductKindId,
                    "skus", List.of(
                            Map.of(
                                    "name", "40",
                                    "price", Map.of(
                                            "amount", 100,
                                            "currency", yuanCurrencyId
                                    ),
                                    "inventory", Map.of(
                                            "quantity", 100
                                    )
                            )
                    )
            ));
        }
    }

    private static void request(String path, Object request) {
        URI uri;
        try {
            uri = new URI(host + path);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header(Headers.APP_ID, Long.toString(appId))
                .PUT(
                        request != null ?
                                HttpRequest.BodyPublishers.ofString(NncUtils.toJSONString(request)) :
                                HttpRequest.BodyPublishers.noBody()
                )
                .build();
        try {
            var resp = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if(resp.statusCode() != 200)
                failureCounter.incrementAndGet();
//            System.out.println(resp.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
