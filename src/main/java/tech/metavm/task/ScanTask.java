package tech.metavm.task;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.object.instance.Instance;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

import java.util.List;

@EntityType("实例扫描任务")
public abstract class ScanTask extends Task {

    public static final long BATCH_SIZE = 256L;

    @EntityField("游标")
    private Instance cursor = InstanceUtils.nullInstance();

    protected ScanTask(String title) {
        super(title);
    }

    public final void processNewInstances(List<Instance> newInstances, IInstanceContext context) {
        process(newInstances, context);
    }

    @Override
    protected boolean run0(IInstanceContext context) {
        List<Instance> batch = scan(context, cursor, BATCH_SIZE);
        if(NncUtils.isEmpty(batch)) {
            onScanOver();
            return true;
        }
        process(batch, context);
        if(batch.size() >= BATCH_SIZE) {
            cursor = batch.get(batch.size() - 1);
            return false;
        }
        else {
            onScanOver();
            return true;
        }
    }

    protected void onScanOver() {}

    protected abstract List<Instance> scan(IInstanceContext context,
                                           Instance cursor,
                                           @SuppressWarnings("SameParameterValue") long limit);

    protected abstract void process(List<Instance> batch, IInstanceContext context);

}
