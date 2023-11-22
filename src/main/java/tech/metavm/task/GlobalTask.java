package tech.metavm.task;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.tenant.TenantRT;

import java.util.List;

@EntityType("全局任务")
public abstract class GlobalTask extends EntityScanTask<TenantRT> {

    protected GlobalTask(String title) {
        super(title, TenantRT.class);
    }

    @Override
    protected void processModels(IInstanceContext context, List<TenantRT> tenants) {
        for (TenantRT tenant : tenants) {
            IEntityContext tenantContext = context.createSame(tenant.getIdRequired()).getEntityContext();
            processTenant(tenantContext, tenant);
            tenantContext.finish();
        }
    }

    protected abstract void processTenant(IEntityContext context, TenantRT tenant);

}
