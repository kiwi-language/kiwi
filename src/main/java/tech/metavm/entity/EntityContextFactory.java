package tech.metavm.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.infra.IdService;
import tech.metavm.util.ContextUtil;

@Component
public class EntityContextFactory {

    @Autowired
    private StoreRegistry storeRegistry;

    @Autowired
    private IdService idService;

    public EntityContext newContext(long tenantId) {
        return new EntityContext(tenantId, storeRegistry, idService);
    }

    public EntityContext newContext() {
        return newContext(ContextUtil.getTenantId());
    }

}
