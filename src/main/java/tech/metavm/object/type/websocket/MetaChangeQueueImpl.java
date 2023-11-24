package tech.metavm.object.type.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tech.metavm.object.type.websocket.dto.MetaChangeMessage;
import tech.metavm.util.ContextUtil;

@Component
public class MetaChangeQueueImpl implements MetaChangeQueue {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public MetaChangeQueueImpl(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    public void sendMetaChange(long tenantId, long version) {
        try(var ignored = ContextUtil.getProfiler().enter("sendMetaChange")) {
            simpMessagingTemplate.convertAndSend(
                    String.format("/topic/meta-change/%d", tenantId),
                    new MetaChangeMessage(tenantId, version)
            );
        }
    }

}
