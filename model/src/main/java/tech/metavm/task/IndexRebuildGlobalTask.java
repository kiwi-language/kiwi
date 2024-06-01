package tech.metavm.task;

import tech.metavm.application.Application;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;

@EntityType("索引重建全局任务")
public class IndexRebuildGlobalTask extends GlobalTask {

    public IndexRebuildGlobalTask() {
        super("Index rebuild boot");
    }

    @Override
    protected void processApplication(IEntityContext context, Application application) {
        context.bind(new IndexRebuildTask());
    }
}
