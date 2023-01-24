package tech.metavm.job;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.object.instance.Instance;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

import java.util.List;

@EntityType("实例扫描任务")
public abstract class InstanceScanJob extends Job {

    public static final long BATCH_SIZE = 256L;

    private Instance cursor = InstanceUtils.nullInstance();

    protected InstanceScanJob(String title) {
        super(title);
    }

    @Override
    protected boolean run0(IInstanceContext context) {
        List<Instance> batch = scan(context, cursor, BATCH_SIZE);
        if(NncUtils.isEmpty(batch)) {
            return true;
        }
        process(context, batch);
        if(batch.size() >= BATCH_SIZE) {
            cursor = batch.get(batch.size() - 1);
            return false;
        }
        else {
            return true;
        }
    }

    protected abstract List<Instance> scan(IInstanceContext context,
                                           Instance cursor,
                                           @SuppressWarnings("SameParameterValue") long limit);

    protected abstract void process(IInstanceContext context, List<Instance> batch);

}
