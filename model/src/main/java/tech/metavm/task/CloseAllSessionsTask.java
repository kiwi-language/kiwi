package tech.metavm.task;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.core.Id;
import tech.metavm.user.Session;

@EntityType("关闭全部会话任务")
public class CloseAllSessionsTask extends Task {

    private final String appId;

    public CloseAllSessionsTask(String title, String appId) {
        super(title);
        this.appId = appId;
    }

    @Override
    protected boolean run0(IEntityContext platformContext) {
        try (var context = platformContext.createSame(Id.parse(appId))) {
            var sessions = context.getByType(Session.class, null, BATCH_SIZE);
            sessions.forEach(Session::close);
            context.finish();
            return sessions.size() < BATCH_SIZE;
        }
    }
}
