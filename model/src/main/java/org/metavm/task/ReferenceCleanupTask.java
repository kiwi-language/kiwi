package org.metavm.task;

import org.metavm.entity.EntityType;
import org.metavm.entity.EntityUtils;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;

@EntityType
public class ReferenceCleanupTask extends Task {

    public static final long BATCH_SIZE = 256;

    private final String targetId;
    private long nextTreeId;

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
        var instances = instanceContext.getByReferenceTargetId(Id.parse(targetId), nextTreeId, BATCH_SIZE);
        for (Instance instance : instances) {
            EntityUtils.ensureProxyInitialized(instance);
        }
        if(instances.size() < BATCH_SIZE) {
            return true;
        }
        nextTreeId = instances.get(instances.size() - 1).getTreeId();
        return false;
    }
    
}
