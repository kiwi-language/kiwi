package org.metavm.task;

import org.metavm.api.Entity;
import org.metavm.entity.IEntityContext;
import org.metavm.event.rest.dto.ReceiveMessageEvent;
import org.metavm.message.Message;
import org.metavm.util.Hooks;

@Entity
public class SendMessageTask extends Task {

    private final Message message;

    public SendMessageTask(Message message) {
        super("SendMessageTask");
        this.message = message;
    }

    @Override
    protected boolean run0(IEntityContext context, IEntityContext taskContext) {
        Hooks.PUBLISH_USER_EVENT.accept(new ReceiveMessageEvent(message.toDTO()));
        return true;
    }
}
