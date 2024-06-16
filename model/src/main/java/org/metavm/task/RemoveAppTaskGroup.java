package org.metavm.task;

import org.metavm.application.Application;
import org.metavm.api.EntityType;
import org.metavm.entity.IEntityContext;

import java.util.List;

@EntityType
public class RemoveAppTaskGroup extends TaskGroup {

    private final String appId;

    public RemoveAppTaskGroup(String appId) {
        this.appId = appId;
    }

    @Override
    public List<Task> createTasks(IEntityContext context) {
        var app = context.getEntity(Application.class, appId);
        return List.of(
                new ClearUsersTask(String.format("Clear users for '%s'", app.getName()), appId),
                new ClearInvitationTask(String.format("Clear invitations for '%s'", app.getName()), appId)
        );
    }

    @Override
    protected void onTasksDone(IEntityContext context) {
        context.remove(context.getEntity(Application.class, appId));
    }
}
