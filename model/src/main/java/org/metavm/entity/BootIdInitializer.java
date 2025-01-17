package org.metavm.entity;

import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.BootIdProvider;

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
    public void initializeIds(long appId, Collection<? extends Instance> instances) {
        List<Instance> toInitialize = new ArrayList<>();
        for (Instance instance : instances) {
            if(instance instanceof Entity entity) {
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
