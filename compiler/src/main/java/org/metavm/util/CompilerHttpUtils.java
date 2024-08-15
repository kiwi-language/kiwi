package org.metavm.util;

import org.metavm.autograph.HttpTypeClient;
import org.metavm.common.Result;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.rest.GetTreesRequest;
import org.metavm.user.rest.dto.LoginInfo;
import org.metavm.user.rest.dto.LoginRequest;

import javax.annotation.Nullable;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class CompilerHttpUtils {

    private static String host = Constants.DEFAULT_HOST;
    private final static HttpClient client;
    private static long appId;

    static {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
                .cookieHandler(CookieHandler.getDefault())
                .build();
    }

    public static void setToken(long appId, String token) {
        var cookie = new HttpCookie(getTokenCookieName(appId), token);
        cookie.setPath("/");
        getCookieManager().getCookieStore().add(URI.create(host), cookie);
    }

    public static String getToken() {
        return getToken(appId);
    }

    public static List<HttpCookie> getCookies() {
        return getCookieManager().getCookieStore().getCookies();
    }

    public static String getToken(long appId) {
        var cookieName = getTokenCookieName(appId);
        var cookieManager = getCookieManager();
        return NncUtils.findRequired(
                        cookieManager.getCookieStore().getCookies(), c -> c.getName().equals(cookieName),
                        "Token not available"
                )
                .getValue();
    }

    private static CookieManager getCookieManager() {
        //noinspection OptionalGetWithoutIsPresent
        return (CookieManager) client.cookieHandler().get();
    }

    private static String getTokenCookieName() {
        return getTokenCookieName(appId);
    }

    private static String getTokenCookieName(long appId) {
        return String.format("__token_%d__", appId);
    }

    public static <R> R delete(String path, TypeReference<R> responseTypeRef) {
        try {
            var uri = new URI(host + path);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header(Headers.APP_ID, Long.toString(appId))
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
                    .header(Headers.APP_ID, Long.toString(appId))
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
                    .header(Headers.APP_ID, Long.toString(appId))
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
            throw new InternalException(result.getMessage());
        return result.getData();
    }

    public static String getHost() {
        return host;
    }

    public static void setHost(String host) {
        CompilerHttpUtils.host = host;
    }

    public static void setAppId(long appId) {
        CompilerHttpUtils.appId = appId;
    }

    public static long getAppId() {
        return appId;
    }

    public static void main(String[] args) {
        testDirectAccess();
//        testLogin();
    }

    private static void testLogin() {
        CompilerHttpUtils.setAppId(2L);
        var loginName = "15968879210@163.com";
        var password = "123456";
        CompilerHttpUtils.post("/login", new LoginRequest(Constants.PLATFORM_APP_ID, loginName, password),
                new TypeReference<LoginInfo>() {
                });
        var appId = 1000400011L;
        CompilerHttpUtils.post("/platform-user/enter-app/" + appId, null, new TypeReference<LoginInfo>() {
        });
        System.out.println(CompilerHttpUtils.getToken(2));
        System.out.println(CompilerHttpUtils.getToken(appId));
        CompilerHttpUtils.setAppId(appId);
        printCookies();
        testAccess();
    }

    private static void testDirectAccess() {
        var token = "06581877-42f3-43e2-8114-63f02c51a7ef";
        var appId = 1000400011L;
        CompilerHttpUtils.setAppId(appId);
        CompilerHttpUtils.setToken(appId, token);
        printCookies();
        testAccess();
    }

    private static void printCookies() {
        CompilerHttpUtils.getCookies().forEach(c -> System.out.printf("%s = %s%n",
                c.getName(), c.getValue()));
    }

    private static void testAccess() {
        var typeClient = new HttpTypeClient();
        var id = "02b2fafab9070028";
        var treeId = Id.parse(id).getTreeId();
        var trees = typeClient.getTrees(new GetTreesRequest(List.of(treeId)));
        System.out.println(NncUtils.toPrettyJsonString(trees));
    }

}
