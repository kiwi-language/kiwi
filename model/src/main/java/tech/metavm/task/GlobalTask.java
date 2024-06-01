package tech.metavm.task;

import tech.metavm.application.Application;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;

import java.util.List;

@EntityType("全局任务")
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
