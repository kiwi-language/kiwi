package tech.metavm.task;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.application.Application;

import java.util.List;

@EntityType("全局任务")
public abstract class GlobalTask extends EntityScanTask<Application> {

    protected GlobalTask(String title) {
        super(title, Application.class);
    }

    @Override
    protected void processModels(IInstanceContext context, List<Application> applications) {
        for (Application application : applications) {
            IEntityContext appContext = context.createSame(application.getIdRequired()).getEntityContext();
            processApplication(appContext, application);
            appContext.finish();
        }
    }

    protected abstract void processApplication(IEntityContext context, Application application);

}
