package org.metavm.task;

import org.metavm.application.Application;
import org.metavm.entity.EntityType;
import org.metavm.entity.IEntityContext;

import java.util.List;

@EntityType
public abstract class GlobalTask extends EntityScanTask<Application> {

    protected GlobalTask(String title) {
        super(title, Application.class);
    }

    @Override
    protected void processModels(IEntityContext context, List<Application> applications) {
        for (Application application : applications) {
            var appContext = context.createSame(application.getTreeId());
            processApplication(appContext, application);
            appContext.finish();
        }
    }

    protected abstract void processApplication(IEntityContext context, Application application);

}
