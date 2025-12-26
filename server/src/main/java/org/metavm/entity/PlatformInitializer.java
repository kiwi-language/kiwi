package org.metavm.entity;

import org.metavm.application.Application;
import org.metavm.context.Component;
import org.metavm.context.InitializingBean;
import org.metavm.jdbc.MockTransactionUtils;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.type.GlobalKlassTagAssigner;
import org.metavm.task.SchedulerRegistry;
import org.metavm.user.PlatformUser;
import org.metavm.util.Constants;

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
