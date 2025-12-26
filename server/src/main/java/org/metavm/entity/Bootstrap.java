package org.metavm.entity;

import org.metavm.context.Component;
import org.metavm.context.InitializingBean;
import org.metavm.context.sql.Transactional;
import org.metavm.object.type.GlobalKlassTagAssigner;
import org.metavm.object.type.StdAllocators;
import org.metavm.task.SchedulerRegistry;

@Component
public class Bootstrap extends EntityContextFactoryAware implements InitializingBean {

    private final StdAllocators stdAllocators;

    public Bootstrap(EntityContextFactory entityContextFactory, StdAllocators stdAllocators) {
        super(entityContextFactory);
        this.stdAllocators = stdAllocators;
    }

    public BootstrapResult boot() {
        // Ensure initialized
        StdMethod.values();
        var result = Bootstraps.boot(stdAllocators, true);
        entityContextFactory.setDefContext(result.defContext());
        return result;
    }

    @Transactional
    public void initSystemEntities() {
        try(var context = newPlatformContext()) {
            SchedulerRegistry.initialize(context);
            GlobalKlassTagAssigner.initialize(context);
            context.finish();
        }
    }

    @Override
    public void afterPropertiesSet() {
        boot();
    }
}
