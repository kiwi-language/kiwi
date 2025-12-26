package org.metavm.task;

import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.event.rest.dto.ReceiveMessageEvent;
import org.metavm.message.Message;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.Hooks;

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
