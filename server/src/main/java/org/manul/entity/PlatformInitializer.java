package org.manul.entity;

import org.manul.application.ApplicationManager;
import org.manul.context.Component;
import org.manul.context.Init;
import org.manul.context.sql.Transactional;
import org.manul.object.instance.persistence.SchemaManager;
import org.manul.object.type.GlobalKlassTagAssigner;
import org.manul.task.SchedulerRegistry;
import org.manul.user.PlatformUserManager;
import org.manul.user.rest.dto.UserDTO;
import org.manul.util.Constants;

import java.util.List;

@Component
public class PlatformInitializer extends EntityContextFactoryAware {

    private final SchemaManager schemaManager;
    private final PlatformUserManager userManager;
    private final ApplicationManager applicationManager;

    public PlatformInitializer(EntityContextFactory entityContextFactory, SchemaManager schemaManager, PlatformUserManager userManager, ApplicationManager applicationManager) {
        super(entityContextFactory);
        this.schemaManager = schemaManager;
        this.userManager = userManager;
        this.applicationManager = applicationManager;
    }

    @Init(order = 1000)
    @Transactional
    public void init() {
        if (schemaManager.instanceTableExists(1, "instance"))
            return;
        applicationManager.createPlatform();
        applicationManager.createRoot();
        try (var platformContext = entityContextFactory.newContext(Constants.PLATFORM_APP_ID)) {
            SchedulerRegistry.initialize(platformContext);
            GlobalKlassTagAssigner.initialize(platformContext);
            platformContext.finish();
        }
        userManager.save(new UserDTO(null, Constants.DEFAULT_USER, Constants.DEFAULT_USER, "", List.of()));
    }
}
