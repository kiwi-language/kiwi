package tech.metavm.task;

import tech.metavm.application.Application;
import tech.metavm.entity.*;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.user.PlatformUser;
import tech.metavm.user.PlatformUsers;

@EntityType("清空用户任务")
public class ClearUsersTask extends Task {

    @EntityField("应用ID")
    private final long appId;

    protected ClearUsersTask(String title, long appId) {
        super(title);
        this.appId = appId;
    }

    @Override
    protected boolean run0(IInstanceContext context) {
        var app = context.getEntityContext().getEntity(Application.class, appId);
        var users = context.getEntityContext().query(
                PlatformUser.IDX_APP.newQueryBuilder()
                        .addEqItem("applications", app)
                        .limit(BATCH_SIZE)
                        .build()
        );
        PlatformUsers.leaveApp(users, app, context.getEntityContext());
        return users.size() < BATCH_SIZE;
    }
}
