package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.InstanceReference;
import org.metavm.util.ContextUtil;
import org.metavm.util.NncUtils;

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
        var batch = scan(context.getInstanceContext(), cursor, BATCH_SIZE);
        if (NncUtils.isEmpty(batch)) {
            onScanOver(context, taskContext);
            return true;
        }
        process(batch, context, taskContext);
        if (batch.size() >= BATCH_SIZE) {
            cursor = batch.get(batch.size() - 1).getTreeId();
            return false;
        } else {
            onScanOver(context, taskContext);
            return true;
        }
    }

    protected void onScanOver(IEntityContext context, IEntityContext taskContext) {}

    protected abstract List<InstanceReference> scan(IInstanceContext context,
                                                    long cursor,
                                                    @SuppressWarnings("SameParameterValue") long limit);

    protected abstract void process(List<InstanceReference> batch, IEntityContext context, IEntityContext taskContext);

}
