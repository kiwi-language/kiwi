package org.metavm.chat;


import com.google.genai.Client;
import com.google.genai.types.*;

import java.util.Objects;
import java.util.Scanner;

public class GeminiDemo {

    public static final String model = "gemini-2.5-flash-preview-05-20";

    public static void main(String[] args) {

        // The client gets the API key from the environment variable `GOOGLE_API_KEY`.
        Client client = Client.builder()
                .apiKey("AIzaSyAj5ppdmrHTJ-MF59GxLe96jcQj1g1xE3g")
                .build();
        var scanner = new Scanner(System.in);
        var chat = client.chats.create(model, GenerateContentConfig.builder()
                .thinkingConfig(
                        ThinkingConfig.builder()
                                .includeThoughts(true)
                                .build()
                )
                .build());
        for (; ; ) {
            System.out.print("Input: ");
            var msg = scanner.nextLine();
            var s = chat.sendMessage(msg);
            for (Part part : Objects.requireNonNull(s.parts())) {
                if (part.thought().orElse(false)) {
                    System.out.println("----Thought----");
                    System.out.println(part.text().orElse(""));
                } else {
                    System.out.println("----Answer----");
                    System.out.println(part.text().orElse(""));
                }
            }
        }
    }

}
