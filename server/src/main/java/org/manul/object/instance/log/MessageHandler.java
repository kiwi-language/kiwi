package org.manul.object.instance.log;

import org.manul.entity.EntityContextFactory;
import org.manul.object.instance.core.IInstanceContext;
import org.manul.message.Message;
import org.manul.task.SendMessageTask;
import org.manul.context.Component;

import javax.annotation.Nullable;
import java.util.List;

@Component
public class MessageHandler implements LogHandler<Message> {

    @Override
    public Class<Message> getEntityClass() {
        return Message.class;
    }

    @Override
    public void process(List<Message> created, @Nullable String clientId, IInstanceContext context, EntityContextFactory entityContextFactory) {
        for (Message message : created) {
            context.bind(new SendMessageTask(context.allocateRootId(), message));
        }
    }
}
