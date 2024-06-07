package tech.metavm.task;

import tech.metavm.application.Application;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityIndexKey;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.user.PlatformUser;
import tech.metavm.user.PlatformUsers;

import java.util.List;

@EntityType
public class ClearUsersTask extends Task {

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
                        .eq(new EntityIndexKey(List.of(app)))
                        .limit(BATCH_SIZE)
                        .build()
        );
        PlatformUsers.leaveApp(users, app, context);
        return users.size() < BATCH_SIZE;
    }
}
