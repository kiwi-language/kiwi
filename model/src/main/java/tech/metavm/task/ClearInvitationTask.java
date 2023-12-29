package tech.metavm.task;

import tech.metavm.application.AppInvitation;
import tech.metavm.application.Application;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.message.Message;

@EntityType("删除邀请任务")
public class ClearInvitationTask extends Task {

    private final long appId;

    public ClearInvitationTask(String title, long appId) {
        super(title);
        this.appId = appId;
    }

    @Override
    protected boolean run0(IEntityContext context) {
        var app = context.getEntity(Application.class, appId);
        var invitations = context.query(AppInvitation.IDX_APP.newQueryBuilder()
                .addEqItem("application", app)
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
