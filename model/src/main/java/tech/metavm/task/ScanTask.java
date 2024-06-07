package tech.metavm.task;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.util.NncUtils;

import java.util.List;

@EntityType
public abstract class ScanTask extends Task {

    public static final long BATCH_SIZE = 256L;

    private long cursor;

    protected ScanTask(String title) {
        super(title);
    }

    public final void processNewInstances(List<DurableInstance> newInstances, IEntityContext context) {
        process(newInstances, context);
    }

    @Override
    protected boolean run0(IEntityContext context) {
        var batch = scan(context.getInstanceContext(), cursor, BATCH_SIZE);
        if(NncUtils.isEmpty(batch)) {
            onScanOver();
            return true;
        }
        process(batch, context);
        if(batch.size() >= BATCH_SIZE) {
            cursor = batch.get(batch.size() - 1).getTreeId();
            return false;
        }
        else {
            onScanOver();
            return true;
        }
    }

    protected void onScanOver() {}

    protected abstract List<DurableInstance> scan(IInstanceContext context,
                                                  long cursor,
                                                  @SuppressWarnings("SameParameterValue") long limit);

    protected abstract void process(List<DurableInstance> batch, IEntityContext context);

}
