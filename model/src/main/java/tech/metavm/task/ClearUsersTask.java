package tech.metavm.task;

import tech.metavm.application.Application;
import tech.metavm.entity.*;
import tech.metavm.user.PlatformUser;
import tech.metavm.user.PlatformUsers;

@EntityType("清空用户任务")
public class ClearUsersTask extends Task {

    @EntityField("应用ID")
    private final String appId;

    protected ClearUsersTask(String title, String appId) {
        super(title);
        this.appId = appId;
    }

    @Override
    protected boolean run0(IEntityContext context) {
        var app = context.getEntity(Application.class, appId);
        var users = context.query(
                PlatformUser.IDX_APP.newQueryBuilder()
                        .addEqItem("applications", app)
                        .limit(BATCH_SIZE)
                        .build()
        );
        PlatformUsers.leaveApp(users, app, context);
        return users.size() < BATCH_SIZE;
    }
}
