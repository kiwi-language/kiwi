package org.metavm.task;

import org.metavm.api.Entity;
import org.metavm.application.Application;
import org.metavm.wire.Wire;
import org.metavm.entity.EntityIndexKey;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.user.PlatformUser;
import org.metavm.user.PlatformUsers;

import java.util.List;
import java.util.function.Consumer;

@Wire(30)
@Entity
public class ClearUsersTask extends Task {

    private final Id appId;

    protected ClearUsersTask(Id id, String title, Id appId) {
        super(id, title);
        this.appId = appId;
    }

    @Override
    protected boolean run1(IInstanceContext context, IInstanceContext taskContext) {
        var app = context.getEntity(Application.class, appId);
        var users = context.query(
                PlatformUser.IDX_APP.newQueryBuilder()
                        .eq(new EntityIndexKey(List.of(context.createReference(appId))))
                        .limit(BATCH_SIZE)
                        .build()
        );
        PlatformUsers.leaveApp(users, app, context);
        return users.size() < BATCH_SIZE;
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
