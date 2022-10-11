package tech.metavm.object.instance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.entity.EntityContext;
import tech.metavm.util.ContextUtil;

@Component
public class InstanceContextFactory {

    @Autowired
    private InstanceStore instanceStore;

    public InstanceContext createContext(EntityContext entityContext) {
        return createContext(ContextUtil.getTenantId(), entityContext);
    }

    public InstanceContext createContext(long tenantId, EntityContext entityContext) {
        return new InstanceContext(tenantId, true, instanceStore, entityContext);
    }

}
