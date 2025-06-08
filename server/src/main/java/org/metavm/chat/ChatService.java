package org.metavm.chat;

import lombok.extern.slf4j.Slf4j;
import org.metavm.chat.rest.dto.ChatRequest;
import org.metavm.chat.rest.dto.ChatResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
@Component
public class ChatService {

    private final Agent agent;
    private final String prompt;
    private final DeployService deployService;

    public ChatService(Agent agent, DeployService deployService) {
        this.agent = agent;
        this.deployService = deployService;
        this.prompt = loadPrompt();
    }

    private String loadPrompt() {
        try (var input = ChatService.class.getResourceAsStream("/prompt/prompt.md")
        ) {
            return new String(Objects.requireNonNull(input).readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ChatResponse chat(ChatRequest request) {
        var code = agent.send(buildPrompt(request));
        log.info("Generated code:");
        log.info("{}", code);
        deployService.deploy(request.appId(), code);
        return new ChatResponse(code);
    }

    private String buildPrompt(ChatRequest request) {
        return prompt + request.content();
    }

}
