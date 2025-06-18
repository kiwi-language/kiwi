package org.metavm.lab;

import org.metavm.user.rest.dto.LoginRequest;
import org.metavm.util.Headers;
import org.metavm.util.Utils;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpClientLab {

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        var client =  HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
                .cookieHandler(CookieHandler.getDefault())
                .build();

        URI uri = new URI("http://localhost:8080/login");

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header(Headers.X_APP_ID, Long.toString(2L))
                .POST(HttpRequest.BodyPublishers.ofString(
                        Utils.toJSONString(new LoginRequest(2L, "demo", "123456"))
                ))
                .build();

        var resp = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println(resp);
    }

}
