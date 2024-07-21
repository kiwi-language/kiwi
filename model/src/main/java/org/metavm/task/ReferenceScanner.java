package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.Id;

import java.util.List;

@EntityType
public abstract class ReferenceScanner extends Task {
    private final String targetId;
    private long next;

    protected ReferenceScanner(String title, String targetId) {
        super(title);
        this.targetId = targetId;
    }

    @Override
    protected boolean run0(IEntityContext context, IEntityContext taskContext) {
        var id = Id.parse(this.targetId);
        var referring = context.getInstanceContext().getByReferenceTargetId(id, next, BATCH_SIZE);
        if (!referring.isEmpty()) {
            process(referring);
            next = referring.get(referring.size() - 1).getTreeId() + 1;
        }
        if (referring.size() < BATCH_SIZE) {
            onTaskDone(context, id);
            return true;
        } else
            return false;
    }

    protected abstract void process(List<DurableInstance> batch);

    protected void onTaskDone(IEntityContext context, Id id) {
    }

    public Id getTargetId() {
        return Id.parse(targetId);
    }
}