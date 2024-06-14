package org.metavm.event;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.metavm.event.rest.dto.AppEvent;
import org.metavm.event.rest.dto.UserEvent;
import org.metavm.util.ContextUtil;

@Component
public class EventQueueImpl implements EventQueue {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public EventQueueImpl(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    public void publishUserEvent(UserEvent event) {
        try(var ignored = ContextUtil.getProfiler().enter("publishUserEvent")) {
            simpMessagingTemplate.convertAndSend(String.format("/topic/user/%s", event.getUserId()), event);
        }
    }

    @Override
    public void publishAppEvent(AppEvent event) {
        try(var ignored = ContextUtil.getProfiler().enter("publishAppEvent")) {
            simpMessagingTemplate.convertAndSend(String.format("/topic/app/%d", event.getAppId()), event);
        }
    }
}
