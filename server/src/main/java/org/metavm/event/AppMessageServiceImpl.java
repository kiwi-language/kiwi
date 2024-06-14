package org.metavm.event;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.metavm.message.Message;
import org.metavm.util.ContextUtil;

@Component
public class AppMessageServiceImpl implements AppMessageService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public AppMessageServiceImpl(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    public void sendMessage(Message message) {
        try(var ignored = ContextUtil.getProfiler().enter("sendMessage")) {
            simpMessagingTemplate.convertAndSend(
                    String.format("/topic/app-message/%d", message.getReceiver().tryGetId()),
                    message.toDTO()
            );
        }
    }

}
