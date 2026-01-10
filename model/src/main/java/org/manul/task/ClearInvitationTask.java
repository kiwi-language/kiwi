package org.manul.task;

import org.manul.api.Entity;
import org.manul.application.AppInvitation;
import org.manul.wire.Wire;
import org.manul.entity.EntityIndexKey;
import org.manul.message.Message;
import org.manul.object.instance.core.IInstanceContext;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;

import java.util.List;
import java.util.function.Consumer;

@Wire(43)
@Entity
public class ClearInvitationTask extends Task {

    private final Id appId;

    public ClearInvitationTask(Id id, String title, Id appId) {
        super(id, title);
        this.appId = appId;
    }

    @Override
    protected boolean run1(IInstanceContext context, IInstanceContext taskContext) {
        var invitations = context.query(AppInvitation.IDX_APP.newQueryBuilder()
                .eq(new EntityIndexKey(List.of(context.createReference(appId))))
                .limit(BATCH_SIZE)
                .build());
        for (AppInvitation invitation : invitations) {
            var messages = context.selectByKey(Message.IDX_TARGET, invitation.getReference());
            messages.forEach(Message::clearTarget);
        }
        return invitations.size() < BATCH_SIZE;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

}
