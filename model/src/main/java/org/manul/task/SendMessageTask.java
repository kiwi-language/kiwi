package org.manul.task;

import org.manul.api.Entity;
import org.manul.wire.Wire;
import org.manul.event.rest.dto.ReceiveMessageEvent;
import org.manul.message.Message;
import org.manul.object.instance.core.IInstanceContext;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;
import org.manul.util.Hooks;

import java.util.function.Consumer;

@Wire(20)
@Entity
public class SendMessageTask extends Task {

    private Message message;

    public SendMessageTask(Id id, Message message) {
        super(id, "SendMessageTask");
        this.message = message;
    }

    @Override
    protected boolean run1(IInstanceContext context, IInstanceContext taskContext) {
        Hooks.PUBLISH_USER_EVENT.accept(new ReceiveMessageEvent(message.toDTO()));
        return true;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        action.accept(message.getReference());
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
        action.accept(message);
    }

}
