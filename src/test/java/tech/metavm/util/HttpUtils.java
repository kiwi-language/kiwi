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

public class HttpUtils {

    public static final String COOKIE_FILE = "/Users/leen/workspace/object/.idea/httpRequests/http-client.cookies";

    private static final String TOKEN;

    static {
        try (BufferedReader reader = new BufferedReader(new FileReader(COOKIE_FILE))) {
            reader.readLine();
            String line = reader.readLine();
            String[] splits = line.split("\t");
            TOKEN = splits[3];
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

    private static HttpClient buildClient(URI uri) {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        var cookie = new HttpCookie("__token__", TOKEN);
        cookie.setDomain("localhost");
        cookie.setPath("/");

        ((CookieManager) CookieHandler.getDefault()).getCookieStore().add(uri, cookie);

        return HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
                .cookieHandler(CookieHandler.getDefault())
                .build();
    }

}
