package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.IInstanceContext;

import java.util.List;

@EntityType
public class IndexRebuildTask extends ScanTask {

    protected IndexRebuildTask() {
        super("Index rebuild");
    }

    @Override
    protected List<DurableInstance> scan(IInstanceContext context, long cursor, long limit) {
        return context.scan(cursor, limit);
    }

    @Override
    protected void process(List<DurableInstance> batch, IEntityContext context) {
        batch.forEach(DurableInstance::incVersion);
    }

}
