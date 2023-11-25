package tech.metavm.util;

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

public class HttpUtils {

    public static final String COOKIE_FILE = "/Users/leen/workspace/object/.idea/httpRequests/http-client.cookies";

    private static final Map<String, String> TOKENS = new HashMap<>();

    static {
        try (BufferedReader reader = new BufferedReader(new FileReader(COOKIE_FILE))) {
            reader.readLine();
            String line;
            while ((line= reader.readLine()) != null) {
                String[] splits = line.split("\t");
                String host = splits[0], token = splits[3];
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
                    .DELETE()
                    .build();
            var respStr = client.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body();
            return NncUtils.readJSONString(respStr, responseTypeRef);
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
                    .POST(
                            HttpRequest.BodyPublishers.ofString(NncUtils.toJSONString(request))
                    )
                    .build();
            var respStr = client.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body();
            return NncUtils.readJSONString(respStr, responseTypeRef);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static HttpClient buildClient(URI uri) {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        var cookie = new HttpCookie("__token__",
                Objects.requireNonNull(TOKENS.get(uri.getHost())));
        cookie.setDomain(uri.getHost());
        cookie.setPath("/");

        ((CookieManager) CookieHandler.getDefault()).getCookieStore().add(uri, cookie);

        return HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
                .cookieHandler(CookieHandler.getDefault())
                .build();
    }

}
