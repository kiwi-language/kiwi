package tech.metavm.task;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.object.instance.Instance;

import java.util.List;

@EntityType("索引重建任务")
public class IndexRebuildTask extends ScanTask {

    protected IndexRebuildTask() {
        super("Index rebuild");
    }

    @Override
    protected List<Instance> scan(IInstanceContext context, Instance cursor, long limit) {
        return context.scan(cursor, limit);
    }

    @Override
    protected void process(List<Instance> batch, IInstanceContext context) {
        batch.forEach(Instance::incVersion);
    }

}
