package tech.metavm.task;

import tech.metavm.application.Application;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;

@EntityType
public class IndexRebuildGlobalTask extends GlobalTask {

    public IndexRebuildGlobalTask() {
        super("Index rebuild boot");
    }

    @Override
    protected void processApplication(IEntityContext context, Application application) {
        context.bind(new IndexRebuildTask());
    }
}
