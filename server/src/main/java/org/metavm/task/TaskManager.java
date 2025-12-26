package org.metavm.task;

import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.util.Constants;
import org.metavm.util.ContextUtil;
import org.metavm.context.Component;
import org.metavm.context.sql.Transactional;

import java.util.List;

@Component
public class TaskManager extends EntityContextFactoryAware {

    public TaskManager(EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
        ShadowTask.saveShadowTasksHook = this::createShadowTasks;
    }


    @Transactional
    public void addIndexRebuildGlobalTask() {
        try (var context = newPlatformContext()) {
            context.bind(new IndexRebuildGlobalTask(context.allocateRootId()));
            context.finish();
        }
    }

    @Transactional
    public void createShadowTasks(long appId, List<Task> created) {
        try (var platformContext = entityContextFactory.newContext(Constants.PLATFORM_APP_ID, builder -> builder.skipPostProcessing(true));
        var ignored = ContextUtil.getProfiler().enter("createShadowTasks")) {
            platformContext.setDescription("ShadowTask");
            for (Task task : created) {
                platformContext.bind(new ShadowTask(platformContext.allocateRootId(), appId, task.getId(), task.getStartAt()));
            }
            platformContext.finish();
        }
    }

}
