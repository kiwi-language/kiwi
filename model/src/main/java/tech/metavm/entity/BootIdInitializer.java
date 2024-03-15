package tech.metavm.entity;

import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.object.instance.core.TypeId;
import tech.metavm.object.type.BootIdProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BootIdInitializer implements IdInitializer {

    private final BootIdProvider bootIdProvider;
    private final IdentityContext identityContext;
    private final IdInitializer defaultIdInitializer;

    public BootIdInitializer(BootIdProvider bootIdProvider, IdentityContext identityContext) {
        this.bootIdProvider = bootIdProvider;
        defaultIdInitializer = new DefaultIdInitializer(bootIdProvider);
        this.identityContext = identityContext;
    }

    @Override
    public TypeId getTypeId(Id id) {
        return bootIdProvider.getTypeId(id);
    }

    @Override
    public void initializeIds(Id appId, Collection<? extends DurableInstance> instances) {
        List<DurableInstance> toInitialize = new ArrayList<>();
        for (DurableInstance instance : instances) {
            var entity = instance.getMappedEntity();
            if(entity != null) {
                var id = bootIdProvider.getId(identityContext.getModelId(entity));
                if(id != null) {
                    instance.initId(PhysicalId.of(id, instance.getType()));
                    continue;
                }
            }
            toInitialize.add(instance);
        }
        defaultIdInitializer.initializeIds(appId, toInitialize);
    }
}
