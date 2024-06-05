package tech.metavm.util;

import tech.metavm.common.Result;

import javax.annotation.Nullable;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CompilerHttpUtils {

    public static String host = "https://metavm.tech/api";
    private final static HttpClient client;
    private static long appId;

    static {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
                .cookieHandler(CookieHandler.getDefault())
                .build();
    }

    public static <R> R delete(String path, TypeReference<R> responseTypeRef) {
        try {
            var uri = new URI(host + path);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("AppId", Long.toString(appId))
                    .DELETE()
                    .build();
            var respStr = client.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body();
            return processResult(respStr, responseTypeRef);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <R> R post(String path, @Nullable Object request, TypeReference<R> responseTypeRef) {
        try {
            var uri = new URI(host + path);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("AppId", Long.toString(appId))
                    .POST(
                            request != null ?
                                    HttpRequest.BodyPublishers.ofString(NncUtils.toJSONString(request)) :
                                    HttpRequest.BodyPublishers.noBody()
                    )
                    .build();
            var resp = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return processResult(resp.body(), responseTypeRef);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <R> R get(String path, TypeReference<R> responseTypeRef) {
        try {
            var uri = new URI(host + path);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("AppId", Long.toString(appId))
                    .GET()
                    .build();
            var respStr = client.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body();
            return processResult(respStr, responseTypeRef);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <R> R processResult(String responseStr, TypeReference<R> responseTypeRef) {
        //noinspection unchecked
        var result = (Result<R>) NncUtils.readJSONString(responseStr,
                ParameterizedTypeImpl.create(Result.class, responseTypeRef.getGenericType()));
        if (!result.isSuccessful())
            throw new InternalException("Request error: " + result.getMessage());
        return result.getData();
    }

    public static void setAppId(long appId) {
        CompilerHttpUtils.appId = appId;
    }

    public static long getAppId() {
        return appId;
    }
}
