package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.ScanResult;
import org.metavm.util.ContextUtil;

import java.util.List;

@EntityType
public abstract class ScanTask extends Task {

    public static long BATCH_SIZE = 256L;

    private long cursor;

    protected ScanTask(String title) {
        super(title);
    }

    @Override
    protected boolean run0(IEntityContext context, IEntityContext taskContext) {
        ContextUtil.setEntityContext(context);
        var r = scan(context.getInstanceContext(), cursor, BATCH_SIZE);
        var batch = r.instances();
        process(batch, context, taskContext);
        if (!r.completed()) {
            cursor = batch.get(batch.size() - 1).getTreeId();
            return false;
        } else {
            onScanOver(context, taskContext);
            return true;
        }
    }

    protected void onScanOver(IEntityContext context, IEntityContext taskContext) {}

    protected ScanResult scan(IInstanceContext context,
                              long cursor,
                              @SuppressWarnings("SameParameterValue") long limit) {
        return context.scan(cursor, limit);
    }

    protected abstract void process(List<DurableInstance> batch, IEntityContext context, IEntityContext taskContext);

}
