package org.manul;


import lombok.SneakyThrows;

import java.io.FileReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class Lab {

    private static final String host = "http://localhost:8080";
    private static final HttpClient client = HttpClient.newBuilder().build();

    @SneakyThrows
    public static void main(String[] args) {
        var path = "/tmp/files.txt";
        var scanner = new Scanner(new FileReader(path));
        while (scanner.hasNextLine()) {
            var line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                var appId = Long.parseLong(line);
                var uri = new URI(host + "/internal-api/app/update-name");
                var req = HttpRequest.newBuilder()
                        .uri(uri)
                        .header("Content-Type", "application/json")
                        .POST(
                                HttpRequest.BodyPublishers.ofString("""
                                        {
                                            "id": %d,
                                            "newName": "%d"
                                        }
                                        """.formatted(appId, appId))
                        )
                        .build();
                var resp = client.send(req, HttpResponse.BodyHandlers.discarding());
                if (resp.statusCode() == 2024 || resp.statusCode() == 200) {
                    System.out.printf("Application %d processed%n", appId);
                } else {
                    System.err.printf("Failed to process application %d, error code: %d%n", appId, resp.statusCode());
                }
            }
        }
    }

}
