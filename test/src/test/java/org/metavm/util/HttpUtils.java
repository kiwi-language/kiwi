package org.metavm.util;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class HttpUtils {

    public static final String COOKIE_FILE = "/Users/leen/workspace/object/.idea/httpRequests/http-client.cookies";
    public static final String APP_ID_FILE = "/Users/leen/workspace/object/src/test/resources/appId";

    private static final Map<String, String> TOKENS = new HashMap<>();
    private static final long appId;
    private static final String tokenCookieName;

    static {
        try (Scanner scanner = new Scanner(new FileReader(APP_ID_FILE))) {
            appId = scanner.nextLong();
        } catch (IOException e) {
            throw new InternalException("Fail to read app id file", e);
        }
        tokenCookieName = String.format("__token_%d__", appId);
        try (BufferedReader reader = new BufferedReader(new FileReader(COOKIE_FILE))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] splits = line.split("\t");
                String host = splits[0], cookieName = splits[2], token = splits[3];
                if (cookieName.equals(tokenCookieName))
                    TOKENS.put(host, token);
            }
        } catch (IOException e) {
            throw new InternalException("Fail to read cookie file", e);
        }
    }

    public static <R> R delete(String path, TypeReference<R> responseTypeRef) {
        try {
            var uri = new URI("http://localhost:8080" + path);
            var client = buildClient(uri);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header(Headers.APP_ID, Long.toString(appId))
                    .DELETE()
                    .build();
            var respStr = client.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body();
            return Utils.readJSONString(respStr, responseTypeRef);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <R> R post(String path, Object request, TypeReference<R> responseTypeRef) {
        try {
            var uri = new URI("http://localhost:8080" + path);
            var client = buildClient(uri);
            return post(client, uri, request, responseTypeRef);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <R> R post(HttpClient client, URI uri, Object request, TypeReference<R> responseTypeRef) {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header(Headers.APP_ID, Long.toString(appId))
                    .POST(
                            HttpRequest.BodyPublishers.ofString(Utils.toJSONString(request))
                    )
                    .build();
            var respStr = client.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body();
            return Utils.readJSONString(respStr, responseTypeRef);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static HttpClient buildClient(URI uri) {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        var cookie = new HttpCookie(tokenCookieName, Objects.requireNonNull(TOKENS.get(uri.getHost())));
        cookie.setDomain(uri.getHost());
        cookie.setPath("/");

        ((CookieManager) CookieHandler.getDefault()).getCookieStore().add(uri, cookie);

        return HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
                .cookieHandler(CookieHandler.getDefault())
                .build();
    }


}
