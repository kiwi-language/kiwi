package tech.metavm.entity;

import tech.metavm.object.instance.core.DurableInstance;
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
    public void initializeIds(long appId, Collection<? extends DurableInstance> instances) {
        List<DurableInstance> toInitialize = new ArrayList<>();
        for (DurableInstance instance : instances) {
            var entity = instance.getMappedEntity();
            if(entity != null) {
                var modelId = identityContext.getModelId(entity);
                var id = bootIdProvider.getId(modelId);
                if(id != null) {
                    instance.initId(id);
                    var nextNodeId = bootIdProvider.getNextNodeId(modelId);
                    if(nextNodeId != null)
                        instance.setNextNodeId(nextNodeId);
                    continue;
                }
            }
            toInitialize.add(instance);
        }
        defaultIdInitializer.initializeIds(appId, toInitialize);
    }
}
