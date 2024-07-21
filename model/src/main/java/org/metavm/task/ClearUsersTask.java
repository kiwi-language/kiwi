package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.application.Application;
import org.metavm.entity.EntityIndexKey;
import org.metavm.entity.IEntityContext;
import org.metavm.user.PlatformUser;
import org.metavm.user.PlatformUsers;

import java.util.List;

@EntityType
public class ClearUsersTask extends Task {

    private final String appId;

    protected ClearUsersTask(String title, String appId) {
        super(title);
        this.appId = appId;
    }

    @Override
    protected boolean run0(IEntityContext context, IEntityContext taskContext) {
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
