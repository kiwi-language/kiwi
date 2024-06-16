package org.metavm.task;

import org.metavm.application.AppInvitation;
import org.metavm.application.Application;
import org.metavm.entity.EntityIndexKey;
import org.metavm.api.EntityType;
import org.metavm.entity.IEntityContext;
import org.metavm.message.Message;

import java.util.List;

@EntityType
public class ClearInvitationTask extends Task {

    private final String appId;

    public ClearInvitationTask(String title, String appId) {
        super(title);
        this.appId = appId;
    }

    @Override
    protected boolean run0(IEntityContext context) {
        var app = context.getEntity(Application.class, appId);
        var invitations = context.query(AppInvitation.IDX_APP.newQueryBuilder()
                .eq(new EntityIndexKey(List.of(app)))
                .limit(BATCH_SIZE)
                .build());
        for (AppInvitation invitation : invitations) {
            var messages = context.selectByKey(Message.IDX_TARGET, invitation);
            messages.forEach(Message::clearTarget);
            context.remove(invitation);
        }
        return invitations.size() < BATCH_SIZE;
    }

}
