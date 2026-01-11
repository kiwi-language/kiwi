package org.manul.entity;

import org.manul.application.Application;
import org.manul.context.Component;
import org.manul.context.InitializingBean;
import org.manul.jdbc.MockTransactionUtils;
import org.manul.object.instance.core.PhysicalId;
import org.manul.object.type.GlobalKlassTagAssigner;
import org.manul.task.SchedulerRegistry;
import org.manul.user.PlatformUser;
import org.manul.util.Constants;

import java.util.List;
import java.util.UUID;

@Component(module = "memory")
public class PlatformInitializer extends EntityContextFactoryAware implements InitializingBean {

    public PlatformInitializer(EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
    }

    @Override
    public void afterPropertiesSet() {
        MockTransactionUtils.doInTransactionWithoutResult(() -> {
            try (var platformContext = entityContextFactory.newContext(Constants.PLATFORM_APP_ID)) {
                SchedulerRegistry.initialize(platformContext);
                GlobalKlassTagAssigner.initialize(platformContext);
                var platformUser = new PlatformUser(platformContext.allocateRootId(), "platform", UUID.randomUUID().toString(), "platform", List.of());
                platformContext.bind(platformUser);
                platformContext.bind(new Application(PhysicalId.of(Constants.PLATFORM_APP_ID, 0), "platform", platformUser));
                platformContext.finish();
            }
        });

    }
}
