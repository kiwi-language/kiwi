package tech.metavm.task;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.IInstanceContext;

import java.util.List;

@EntityType("索引重建任务")
public class IndexRebuildTask extends ScanTask {

    protected IndexRebuildTask() {
        super("Index rebuild");
    }

    @Override
    protected List<DurableInstance> scan(IInstanceContext context, DurableInstance cursor, long limit) {
        return context.scan(cursor, limit);
    }

    @Override
    protected void process(List<DurableInstance> batch, IEntityContext context) {
        batch.forEach(DurableInstance::incVersion);
    }

}
