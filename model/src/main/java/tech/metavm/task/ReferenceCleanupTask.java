package tech.metavm.task;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EntityUtils;
import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType("引用清理任务")
public class ReferenceCleanupTask extends Task {

    public static final long BATCH_SIZE = 256;

    private final String targetId;
    @Nullable
    private String nextReferenceId;

    public ReferenceCleanupTask(String targetId, String typeName, String instanceTitle) {
        super("Reference cleanup " + instanceTitle + "/" + typeName);
        this.targetId = targetId;
    }

    public String getTargetId() {
        return targetId;
    }

    @Override
    public boolean run0(IEntityContext context) {
        var instanceContext = context.getInstanceContext();
        var next = NncUtils.get(nextReferenceId, id -> instanceContext.get(Id.parse(nextReferenceId)));
        var instances = instanceContext.getByReferenceTargetId(Id.parse(targetId), next, BATCH_SIZE);
        for (Instance instance : instances) {
            EntityUtils.ensureProxyInitialized(instance);
        }
        if(instances.size() < BATCH_SIZE) {
            return true;
        }
        nextReferenceId = NncUtils.get(instances.get(instances.size() - 1).tryGetId(), Id::toString);
        return false;
    }
    
}
