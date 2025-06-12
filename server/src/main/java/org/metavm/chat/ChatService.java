package org.metavm.chat;

import lombok.extern.slf4j.Slf4j;
import org.metavm.chat.rest.dto.ChatRequest;
import org.metavm.chat.rest.dto.ChatResponse;
import org.metavm.common.ErrorCode;
import org.metavm.util.BusinessException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
@Component
public class ChatService {

    private final Agent agent;
    private final String prompt;
    private final String fixPrompt;
    private final AgentCompiler agentCompiler;

    public ChatService(Agent agent, AgentCompiler agentCompiler) {
        this.agent = agent;
        this.agentCompiler = agentCompiler;
        this.prompt = loadPrompt("/prompt/prompt.md");
        this.fixPrompt = loadPrompt("/prompt/fix_prompt.md");
    }

    private String loadPrompt(String file) {
        try (var input = ChatService.class.getResourceAsStream(file)) {
            return new String(Objects.requireNonNull(input).readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ChatResponse chat(ChatRequest request) {
        var chat = agent.createChat();
        var code = chat.send(buildPrompt(request));
        log.info("Generated code:");
        log.info("{}", code);
        var r = agentCompiler.deploy(request.appId(), code);
        if (r.successful())
            return new ChatResponse(code);
        for (int i = 0; i < 5; i++) {
            var fixPrompt = buildFixPrompt(r.output());
            log.info("Trying to fix error with prompt: {}", fixPrompt);
            code = chat.send(fixPrompt);
            log.info("Generated code (Retry #{}): {}", code, i + 1);
            r = agentCompiler.deploy(request.appId(), code);
            if (r.successful())
                return new ChatResponse(code);
        }
        throw new BusinessException(ErrorCode.CODE_GENERATION_FAILED);
    }

    private String buildPrompt(ChatRequest request) {
        return prompt + request.content();
    }

    private String buildFixPrompt(String buildOutput) {
        return fixPrompt + buildOutput;
    }

    public static void main(String[] args) {
        var chatService = new ChatService(
                new GeminiAgent("AIzaSyAj5ppdmrHTJ-MF59GxLe96jcQj1g1xE3g"),
                new AgentCompiler(in -> "")
        );
        chatService.chat(new ChatRequest(
                3,
                "A complex CRM system"
        ));
    }

}
