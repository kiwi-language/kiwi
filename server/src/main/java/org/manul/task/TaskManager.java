package org.manul.task;

import org.manul.entity.EntityContextFactory;
import org.manul.entity.EntityContextFactoryAware;
import org.manul.util.Constants;
import org.manul.util.ContextUtil;
import org.manul.context.Component;
import org.manul.context.sql.Transactional;

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
            context.bind(new SearchIndexRebuildGlobalTask(context.allocateRootId()));
            context.finish();
        }
    }

    @Transactional
    public void addIndexRebuildTask(long appId) {
        try (var context = newContext(appId)) {
            context.bind(new IndexRebuildTask(context.allocateRootId()));
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
