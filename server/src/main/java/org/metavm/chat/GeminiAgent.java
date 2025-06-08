package org.metavm.chat;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import org.metavm.common.ErrorCode;
import org.metavm.util.BusinessException;

import javax.annotation.Nullable;

public class GeminiAgent implements Agent {

    public static final String model = "gemini-2.5-flash-preview-05-20";

    private final @Nullable Client client;

    public GeminiAgent(String apiKey) {
        if (apiKey != null) {
            client = Client.builder()
                    .apiKey(apiKey)
                    .build();
        }
        else
            client = null;
    }

    @Override
    public String send(String request) {
        if (client == null)
            throw new BusinessException(ErrorCode.GEMINI_NOT_CONFIGURED);
        return client.models.generateContent(model, request, GenerateContentConfig.builder().build()).text();
    }
}
