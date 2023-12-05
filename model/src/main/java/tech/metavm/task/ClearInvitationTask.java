package tech.metavm.task;

import tech.metavm.application.AppInvitation;
import tech.metavm.application.Application;
import tech.metavm.entity.EntityIndexQueryBuilder;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IndexOperator;
import tech.metavm.message.Message;
import tech.metavm.object.instance.core.IInstanceContext;

@EntityType("删除邀请任务")
public class ClearInvitationTask extends Task {

    private final long appId;

    public ClearInvitationTask(String title, long appId) {
        super(title);
        this.appId = appId;
    }

    @Override
    protected boolean run0(IInstanceContext context) {
        var entityContext = context.getEntityContext();
        var app = entityContext.getEntity(Application.class, appId);
        var invitations = entityContext.query(AppInvitation.IDX_APP.newQueryBuilder()
                .addEqItem("application", app)
                .limit(BATCH_SIZE)
                .build());
        for (AppInvitation invitation : invitations) {
            var messages = entityContext.selectByKey(Message.IDX_TARGET, invitation);
            messages.forEach(Message::clearTarget);
            entityContext.remove(invitation);
        }
        return invitations.size() < BATCH_SIZE;
    }

}
