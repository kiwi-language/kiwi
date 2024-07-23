package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.DurableInstance;

import java.util.List;

@EntityType
public class IndexRebuildTask extends ScanTask {

    protected IndexRebuildTask() {
        super("Index rebuild");
    }

    @Override
    protected void process(List<DurableInstance> batch, IEntityContext context, IEntityContext taskContext) {
        batch.forEach(DurableInstance::incVersion);
    }

}
