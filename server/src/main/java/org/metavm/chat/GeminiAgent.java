package org.metavm.chat;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Part;
import com.google.genai.types.ThinkingConfig;
import lombok.extern.slf4j.Slf4j;
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
    public Chat createChat() {
        if (client == null)
            throw new BusinessException(ErrorCode.GEMINI_NOT_CONFIGURED);
        return new GeminiChat(client.chats.create(model, GenerateContentConfig
                .builder()
                .thinkingConfig(
                        ThinkingConfig.builder()
                                .includeThoughts(true)
                                .build()
                )
                .build()));
    }

    @Slf4j
    private static class GeminiChat implements Chat {

        private final com.google.genai.Chat chat;

        public GeminiChat(com.google.genai.Chat chat) {
            this.chat = chat;
        }

        @Override
        public String send(String text) {
            var firstThought = true;
            try (var stream = chat.sendMessageStream(text)) {
                var buf = new StringBuilder();
                for (var resp : stream) {
                    var content = resp.candidates().orElseThrow().getFirst().content().orElseThrow();
                    if (content.parts().isEmpty())
                        continue;
                    var parts = content.parts().get();
                    for (Part part : parts) {
                        if (part.text().isPresent()) {
                            if (part.thought().orElse(false)) {
                                if (firstThought) {
                                    firstThought = false;
                                    log.info("Thinking:");
                                }
                                log.info("{}", part.text().get());
                            } else
                                buf.append(part.text().get());
                        }
                    }
                }
                return buf.toString();
            }
        }
    }


}