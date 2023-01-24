package tech.metavm.job;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.tenant.TenantRT;

@EntityType("索引重建任务")
public class IndexRebuildGlobalJob extends GlobalJob {

    public IndexRebuildGlobalJob() {
        super("Index rebuild boot");
    }

    @Override
    protected void processTenant(IEntityContext context, TenantRT tenant) {
        context.bind(new IndexRebuildJob());
    }
}
