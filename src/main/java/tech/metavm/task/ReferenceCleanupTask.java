package tech.metavm.task;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EntityUtils;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.object.instance.Instance;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

@EntityType("引用清理任务")
public class ReferenceCleanupTask extends Task {

    public static final long BATCH_SIZE = 256;

    private final long targetId;
    @Nullable
    private Long nextReferenceId;

    public ReferenceCleanupTask(long targetId, String typeName, String instanceTitle) {
        super("Reference cleanup " + instanceTitle + "/" + typeName);
        this.targetId = targetId;
    }

    public long getTargetId() {
        return targetId;
    }

    @Override
    public boolean run0(IInstanceContext context) {
        Instance next = NncUtils.get(nextReferenceId, context::get);
        List<Instance> instances = context.getByReferenceTargetId(targetId, next, BATCH_SIZE);
        for (Instance instance : instances) {
            EntityUtils.ensureProxyInitialized(instance);
        }
        if(instances.size() < BATCH_SIZE) {
            return true;
        }
        nextReferenceId = instances.get(instances.size() - 1).getId();
        return false;
    }
    
}
