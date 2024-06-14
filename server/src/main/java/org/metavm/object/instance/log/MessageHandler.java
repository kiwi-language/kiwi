package org.metavm.object.instance.log;

import org.springframework.stereotype.Component;
import org.metavm.entity.IEntityContext;
import org.metavm.event.EventQueue;
import org.metavm.event.rest.dto.ReceiveMessageEvent;
import org.metavm.message.Message;

import javax.annotation.Nullable;
import java.util.List;

@Component
public class MessageHandler implements LogHandler<Message> {

    private final EventQueue eventQueue;

    public MessageHandler(EventQueue eventQueue) {
        this.eventQueue = eventQueue;
    }

    @Override
    public Class<Message> getEntityClass() {
        return Message.class;
    }

    @Override
    public void process(List<Message> created, @Nullable String clientId, IEntityContext context) {
        for (Message message : created) {
            eventQueue.publishUserEvent(new ReceiveMessageEvent(message.toDTO()));
        }
    }
}
