package tech.metavm.task;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.application.Application;

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
