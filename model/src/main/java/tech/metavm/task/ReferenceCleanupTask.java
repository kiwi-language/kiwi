package tech.metavm.task;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EntityUtils;
import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

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
    public boolean run0(IEntityContext context) {
        var instanceContext = context.getInstanceContext();
        var next = NncUtils.get(nextReferenceId, id -> instanceContext.get(new PhysicalId(id)));
        var instances = instanceContext.getByReferenceTargetId(targetId, next, BATCH_SIZE);
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
