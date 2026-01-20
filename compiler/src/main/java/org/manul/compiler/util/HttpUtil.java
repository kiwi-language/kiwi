package org.manul.compiler.util;

import lombok.Getter;
import lombok.SneakyThrows;
import org.jsonk.Jsonk;
import org.jsonk.Type;
import org.manul.application.rest.dto.ApplicationDTO;
import org.manul.common.ErrorResponse;
import org.manul.common.Page;
import org.manul.compiler.HttpTypeClient;
import org.manul.user.rest.dto.LoginInfo;
import org.manul.user.rest.dto.LoginRequest;
import org.manul.util.Constants;
import org.manul.util.Headers;

import javax.annotation.Nullable;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpUtil {

    @Getter
    private static String host = Constants.DEFAULT_HOST;
    private final static HttpClient client;
    @Getter
    private static String token;

    static {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
                .cookieHandler(CookieHandler.getDefault())
                .build();
    }

    public static void setToken(String token) {
        HttpUtil.token = token;
    }

    public static List<HttpCookie> getCookies() {
        return getCookieManager().getCookieStore().getCookies();
    }

    private static CookieManager getCookieManager() {
        //noinspection OptionalGetWithoutIsPresent
        return (CookieManager) client.cookieHandler().get();
    }

    @SneakyThrows
    public static <R> R delete(String path, Class<R> clazz) {
        var uri = new URI(host + path);
        var builder = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header(Headers.X_APP_ID, "2")
                .DELETE();
        if (token != null)
            builder.header("Authorization", "Bearer " + token);
        var resp = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return processResponse(resp, clazz);
    }

    public static LoginInfo login(String userName, String password) {
        var loginInfo = post("/auth/login", new LoginRequest(Constants.PLATFORM_APP_ID, userName, password), LoginInfo.class);
        setToken(loginInfo.token());
        return loginInfo;
    }

    public static void logout() {
        post("/auth/logout", null, void.class);
        setToken(null);
    }

    @SneakyThrows
    public static <R> R post(String path, @Nullable Object request, Class<R> clazz) {
        var uri = new URI(host + path);
        var builder = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header(Headers.X_APP_ID, "2")
                .POST(
                        request != null ?
                                HttpRequest.BodyPublishers.ofString(Jsonk.toJson(request)) :
                                HttpRequest.BodyPublishers.noBody()
                );
        if (token != null)
            builder.header("Authorization", "Bearer " + token);
        var resp = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return processResponse(resp, clazz);
    }

    @SneakyThrows
    public static <R> R upload(String path, String filePath, Class<R> clazz) {
        var builder = HttpRequest.newBuilder()
                .uri(new URI(host + path))
                .header("Accept", "application/json")
                .header("Content-Type", "application/octet-stream")
                .header(Headers.X_APP_ID, "2")
                .POST(HttpRequest.BodyPublishers.ofFile(Paths.get(filePath)));
        if (token != null)
            builder.header("Authorization", "Bearer " + token);
        var resp = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return processResponse(resp, clazz);
    }

    @SneakyThrows
    public static String deploy(long appId, String filePath) {
        var uri = new URI(host + "/internal-api/deploy/" + appId);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .header("Content-Type", "application/octet-stream")
                .POST(HttpRequest.BodyPublishers.ofFile(Paths.get(filePath)))
                .build();
        var resp = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        return processResponse(resp, String.class);
    }

    private static <R> R processResponse(HttpResponse<String> resp, Class<R> clazz) {
        //noinspection unchecked
        return (R) processResponse(resp, Type.from(clazz));
    }

    private static Object processResponse(HttpResponse<String> resp, Type type) {
        if (resp.statusCode() == 204)
            return null;
        if (resp.statusCode() != 200) {
            var errorResp = Jsonk.fromJson(resp.body(), ErrorResponse.class);
            throw new RequestException(errorResp.getMessage());
        }
        if (type.clazz() == Void.class || type.clazz() == void.class)
            return resp.body();
        if (type.clazz() == String.class)
            return resp.body();
        else
            return Jsonk.fromJson(resp.body(), type);
    }

    @SneakyThrows
    public static void revert(long appId) {
        var uri = new URI(host + "/internal-api/deploy/revert/" + appId);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        var resp = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        processResponse(resp, Void.class);
    }

    public static <R> R get(String path, Class<R> clazz) {
        return get(path, clazz, Map.of());
    }

    public static <R> R get(String path, Class<R> clazz, Map<String, String> requestParams) {
        //noinspection unchecked
        return (R) get(path, Type.from(clazz), requestParams);
    }

    public static Object get(String path, Type type) {
        return get(path, type, Map.of());
    }

    @SneakyThrows
    public static Object get(String path, Type type, Map<String, String> requestParams) {
        var url = host + path;
        if (!requestParams.isEmpty()) {
            var query = requestParams.entrySet().stream()
                    .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                    .collect(Collectors.joining("&"));
            url += "?" + query;
        }
        var uri = new URI(url);

        var builder = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header(Headers.X_APP_ID, "2")
                .GET();
        if (token != null)
            builder.header("Authorization", "Bearer " + token);
        var httpRequest = builder.build();
        var resp = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        return processResponse(resp, type);
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    public static void setHost(String host) {
        HttpUtil.host = host;
    }

    public static void main(String[] args) {
//        testDirectAccess();
        testLogin();
    }

    private static void testLogin() {
        var loginName = "demo";
        var password = "123456";
        var loginInfo = login(loginName, password);
        HttpUtil.setToken(loginInfo.token());
        System.out.println(HttpUtil.getToken());
        //noinspection unchecked
        var apps = (Page<ApplicationDTO>) get("/app/search", Type.from(Page.class, ApplicationDTO.class));
        System.out.println(apps);
        printCookies();
        testAccess();
    }

    private static void printCookies() {
        HttpUtil.getCookies().forEach(c -> System.out.printf("%s = %s%n",
                c.getName(), c.getValue()));
    }

    private static void testAccess() {
        var typeClient = new HttpTypeClient();
        System.out.println(typeClient.ping());
    }

}
