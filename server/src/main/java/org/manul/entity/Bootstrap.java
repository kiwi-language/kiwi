package org.manul.entity;

import org.manul.context.Component;
import org.manul.context.Init;
import org.manul.context.sql.Transactional;
import org.manul.object.type.GlobalKlassTagAssigner;
import org.manul.object.type.StdAllocators;
import org.manul.task.SchedulerRegistry;

@Component
public class Bootstrap extends EntityContextFactoryAware {

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

    @Init
    public void init() {
        boot();
    }
}
