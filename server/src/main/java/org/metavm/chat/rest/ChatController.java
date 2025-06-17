package org.metavm.chat.rest;

import org.metavm.chat.ChatService;
import org.metavm.chat.rest.dto.ChatRequest;
import org.metavm.chat.rest.dto.ChatResponse;
import org.metavm.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public Result<ChatResponse> chat(@RequestBody ChatRequest request) {
        return Result.success(chatService.chat(request));
    }

}
