package org.metavm.compiler.util;

import lombok.Getter;
import lombok.SneakyThrows;
import org.jsonk.Jsonk;
import org.jsonk.Type;
import org.metavm.application.rest.dto.ApplicationDTO;
import org.metavm.common.ErrorCode;
import org.metavm.common.ErrorResponse;
import org.metavm.common.Page;
import org.metavm.compiler.HttpTypeClient;
import org.metavm.user.rest.dto.LoginInfo;
import org.metavm.user.rest.dto.LoginRequest;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.util.List;

public class CompilerHttpUtils {

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
        CompilerHttpUtils.token = token;
    }

    public static List<HttpCookie> getCookies() {
        return getCookieManager().getCookieStore().getCookies();
    }

    private static CookieManager getCookieManager() {
        //noinspection OptionalGetWithoutIsPresent
        return (CookieManager) client.cookieHandler().get();
    }

    public static <R> R delete(String path, Class<R> clazz) {
        try {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public static <R> R post(String path, @Nullable Object request, Class<R> clazz) {
        try {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <R> R upload(String path, String filePath, Class<R> clazz) {
        try {
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
        catch (Exception e) {
            throw new RuntimeException(e);
        }
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
            throw new BusinessException(ErrorCode.fromCode(errorResp.getCode()), errorResp.getMessage());
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
        if (resp.statusCode() != 200) {
            var errorResp = Jsonk.fromJson(resp.body(), ErrorResponse.class);
            throw new BusinessException(ErrorCode.DEPLOY_FAILED, errorResp.getMessage());
        }
    }

    public static <R> R get(String path, Class<R> clazz) {
        //noinspection unchecked
        return (R) get(path, Type.from(clazz));
    }

    public static Object get(String path, Type type) {
        try {
            var uri = new URI(host + path);
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <R> R processResult(String responseStr, Class<R> clazz) {
        return Jsonk.fromJson(responseStr, clazz);
    }

    public static void setHost(String host) {
        CompilerHttpUtils.host = host;
    }

    public static void main(String[] args) {
//        testDirectAccess();
        testLogin();
    }

    private static void testLogin() {
        var loginName = "demo";
        var password = "123456";
        var loginInfo = login(loginName, password);
        CompilerHttpUtils.setToken(loginInfo.token());
        System.out.println(CompilerHttpUtils.getToken());
        //noinspection unchecked
        var apps = (Page<ApplicationDTO>) get("/app", Type.from(Page.class, ApplicationDTO.class));
        System.out.println(apps);
        printCookies();
        testAccess();
    }

    private static void testDirectAccess() {
        var token = "06581877-42f3-43e2-8114-63f02c51a7ef";
        CompilerHttpUtils.setToken(token);
        printCookies();
        testAccess();
    }

    private static void printCookies() {
        CompilerHttpUtils.getCookies().forEach(c -> System.out.printf("%s = %s%n",
                c.getName(), c.getValue()));
    }

    private static void testAccess() {
        var typeClient = new HttpTypeClient();
        System.out.println(typeClient.ping());
    }

}
