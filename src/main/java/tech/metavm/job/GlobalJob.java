package tech.metavm.job;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.tenant.TenantRT;

import java.util.List;

@EntityType("全局任务")
public abstract class GlobalJob extends ModelScanJob<TenantRT> {

    protected GlobalJob(String title) {
        super(title, TenantRT.class);
    }

    @Override
    protected void processModels(IInstanceContext context, List<TenantRT> tenants) {
        for (TenantRT tenant : tenants) {
            IEntityContext tenantContext = context.newContext(tenant.getId()).getEntityContext();
            processTenant(tenantContext, tenant);
            tenantContext.finish();
        }
    }

    protected abstract void processTenant(IEntityContext context, TenantRT tenant);

}
