package org.metavm.perf;

import com.fasterxml.jackson.core.type.TypeReference;
import org.metavm.util.Headers;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static final Logger logger = LoggerFactory.getLogger(ApiPerf.class);

    public static final int THREAD_COUNT = 1;
    public static final int EXECUTIONS = 1000;
    private static final String host = "http://localhost:8080/object";
    public static final long appId = 1000001008L;
    public static String defaultProductKindId = "01dab8d6b90700";
    public static String yuanCurrencyId = "01d4b8d6b90700";
    public static String dollarCurrencyId;
    public static String couponActiveStateId;
    private static String productId;
    private static String skuId;

//    private static final String host = "https://api.metavm.tech/object";
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
        prepare();
        run();
    }

    private static void prepare() {
        defaultProductKindId = get("/org/metavm/mlab/ProductKind/DEFAULT");
        yuanCurrencyId = get("/org/metavm/mlab/Currency/YUAN");
        dollarCurrencyId = get("/org/metavm/mlab/Currency/DOLLAR");
        couponActiveStateId = get("/org/metavm/mlab/CouponState/ACTIVE");
        productId = createProduct();
        var product = getObject(productId);
        //noinspection unchecked,rawtypes
        skuId = (String) ((Map<String, Object>)((List)product.get("skus")).getFirst()).get("$id");
    }

    private static void run() {
        var threads = new ArrayList<Thread>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads.add(new Thread(ApiPerf::run0));
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

    private static void run0() {
        for (int i = 0; i < EXECUTIONS; i++) {
            run1();
        }
    }

    private static void run1() {
//        createProduct();
//        getProductName(productId);
        createOrder();
    }

    private static String createOrder() {
        var c1 = createCoupon("c0001", 10, yuanCurrencyId);
        var c2 = createCoupon("d0001", 1, dollarCurrencyId);
        return createOrder(skuId, 1, List.of(c1, c2));
    }

    private static void getProductName(String productId) {
        post("/" + productId + "'/get-name", List.of());
    }

    private static String createOrder(String skuId, int quantity, List<String> coupons) {
        return post("/order-service/create-order", List.of(skuId, quantity, coupons));
    }

    private static String createProduct() {
        return put("/org/metavm/mlab/Product", Map.of(
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
                                        "quantity", 1000000000
                                )
                        )
                )
        ));
    }

    private static Map<String, Object> getObject(String id) {
        return Utils.readJSONString(get("/" + id), new TypeReference<>() {
        });
    }

    private static String createCoupon(String code, double amount, String currency) {
        return put("/org/metavm/mlab/Coupon", Map.of(
                "code", code,
                "state", couponActiveStateId,
                "discount", Map.of(
                        "amount", amount,
                        "currency", currency
                )
        ));
    }

    private static String get(String path) {
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
                .header(Headers.X_APP_ID, Long.toString(appId))
                .GET()
                .build();
        try {
            var resp = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if(resp.statusCode() != 200)
                failureCounter.incrementAndGet();
            return resp.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static String post(String path, Object request) {
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
                .header(Headers.X_APP_ID, Long.toString(appId))
                .POST(
                        request != null ?
                                HttpRequest.BodyPublishers.ofString(Utils.toJSONString(request)) :
                                HttpRequest.BodyPublishers.noBody()
                )
                .build();
        try {
            var resp = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if(resp.statusCode() != 200)
                failureCounter.incrementAndGet();
            return resp.body();
//            System.out.println(resp.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static String put(String path, Object request) {
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
                .header(Headers.X_APP_ID, Long.toString(appId))
                .PUT(
                        request != null ?
                                HttpRequest.BodyPublishers.ofString(Utils.toJSONString(request)) :
                                HttpRequest.BodyPublishers.noBody()
                )
                .build();
        try {
            var resp = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if(resp.statusCode() != 200)
                failureCounter.incrementAndGet();
            return resp.body();
//            System.out.println(resp.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
