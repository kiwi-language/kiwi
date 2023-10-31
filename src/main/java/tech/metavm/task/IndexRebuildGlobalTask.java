package tech.metavm.task;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.tenant.TenantRT;

@EntityType("索引重建全局任务")
public class IndexRebuildGlobalTask extends GlobalTask {

    public IndexRebuildGlobalTask() {
        super("Index rebuild boot");
    }

    @Override
    protected void processTenant(IEntityContext context, TenantRT tenant) {
        context.bind(new IndexRebuildTask());
    }
}
