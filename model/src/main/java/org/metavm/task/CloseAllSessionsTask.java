package org.metavm.task;

import org.metavm.api.Entity;
import org.metavm.entity.IEntityContext;
import org.metavm.user.Session;

@Entity
public class CloseAllSessionsTask extends Task {

    private final long appId;

    private long cursor;

    public CloseAllSessionsTask(String title, long appId) {
        super(title);
        this.appId = appId;
    }

    @Override
    protected boolean run0(IEntityContext platformContext, IEntityContext taskContext) {
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
