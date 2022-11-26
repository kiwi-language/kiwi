package tech.metavm.util;

import tech.metavm.entity.EntityContext;
import tech.metavm.entity.InstanceContextFactory;

public class EntityContextBean {

    private final InstanceContextFactory instanceContextFactory;

    public EntityContextBean(InstanceContextFactory instanceContextFactory) {
        this.instanceContextFactory = instanceContextFactory;
    }

    public EntityContext newContext() {
        return instanceContextFactory.newContext().getEntityContext();
    }
    
}
