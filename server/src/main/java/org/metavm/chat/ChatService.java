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
    private final String createPrompt;
    private final String fixPrompt;
    private final String updatePrompt;
    private final AgentCompiler agentCompiler;

    public ChatService(Agent agent, AgentCompiler agentCompiler) {
        this.agent = agent;
        this.agentCompiler = agentCompiler;
        createPrompt = loadPrompt("/prompt/create.md");
        fixPrompt = loadPrompt("/prompt/fix.md");
        updatePrompt = loadPrompt("/prompt/update.md");
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
        var existingCode = agentCompiler.getCode(request.appId());
        String text;
        if (existingCode != null)
            text = buildUpdateText(request.content(), existingCode);
        else
            text = buildCreateText(request);
        log.info("Initial prompt: {}", text);
        var code = chat.send(text);
        log.info("Generated code:");
        log.info("{}", code);
        var r = agentCompiler.deploy(request.appId(), code);
        if (r.successful())
            return new ChatResponse(code);
        for (int i = 0; i < 5; i++) {
            var fixPrompt = buildFixPrompt(r.output());
            log.info("Trying to fix error with prompt: {}", fixPrompt);
            code = chat.send(fixPrompt);
            log.info("Generated code (Retry #{}):\n{}", i + 1, code);
            r = agentCompiler.deploy(request.appId(), code);
            if (r.successful())
                return new ChatResponse(code);
        }
        throw new BusinessException(ErrorCode.CODE_GENERATION_FAILED);
    }

    private String buildCreateText(ChatRequest request) {
        return createPrompt + request.content();
    }

    private String buildUpdateText(String content, String code) {
        return updatePrompt + content + "\nHere is the existing code:\n" + code;
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
                "I want to you add more fields to the Product"
        ));
    }

}
