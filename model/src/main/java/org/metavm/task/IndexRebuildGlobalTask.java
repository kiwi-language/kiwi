package org.metavm.task;

import org.metavm.application.Application;
import org.metavm.entity.EntityType;
import org.metavm.entity.IEntityContext;

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
