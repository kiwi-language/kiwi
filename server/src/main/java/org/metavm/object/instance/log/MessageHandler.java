package org.metavm.object.instance.log;

import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.IEntityContext;
import org.metavm.message.Message;
import org.metavm.task.SendMessageTask;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;

@Component
public class MessageHandler implements LogHandler<Message> {

    @Override
    public Class<Message> getEntityClass() {
        return Message.class;
    }

    @Override
    public void process(List<Message> created, @Nullable String clientId, IEntityContext context, EntityContextFactory entityContextFactory) {
        for (Message message : created) {
            context.bind(new SendMessageTask(message));
        }
    }
}
