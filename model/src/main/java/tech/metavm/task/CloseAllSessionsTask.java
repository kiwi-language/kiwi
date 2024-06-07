package tech.metavm.task;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.user.Session;

@EntityType
public class CloseAllSessionsTask extends Task {

    private final long appId;

    private long cursor;

    public CloseAllSessionsTask(String title, long appId) {
        super(title);
        this.appId = appId;
    }

    @Override
    protected boolean run0(IEntityContext platformContext) {
        try (var context = platformContext.createSame(appId)) {
            var objects = context.scan(cursor, BATCH_SIZE);
            if(objects.isEmpty())
                return true;
            for (Object object : objects) {
                if(object instanceof Session session) {
                    if(session.isActive())
                        session.close();
                }
            }
            cursor = context.getInstance(objects.get(objects.size() - 1)).getTreeId();
            context.finish();
            return false;
        }
    }
}
