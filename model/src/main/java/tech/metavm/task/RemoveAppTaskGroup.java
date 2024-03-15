package tech.metavm.task;

import tech.metavm.application.Application;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;

import java.util.List;

@EntityType("删除应用任务组")
public class RemoveAppTaskGroup extends TaskGroup {

    @EntityField("应用ID")
    private final String appId;

    public RemoveAppTaskGroup(String appId) {
        this.appId = appId;
    }

    @Override
    public List<Task> createTasks(IEntityContext context) {
        var app = context.getEntity(Application.class, appId);
        return List.of(
                new ClearUsersTask(String.format("清空应用'%s'用户", app.getName()), appId),
                new ClearInvitationTask(String.format("清空应用'%s'邀请", app.getName()), appId)
        );
    }

    @Override
    protected void onTasksDone(IEntityContext context) {
        context.remove(context.getEntity(Application.class, appId));
    }
}
