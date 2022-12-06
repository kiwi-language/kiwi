package tech.metavm.object.instance;

import tech.metavm.entity.EntityChange;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.entity.InstanceContext;
import tech.metavm.object.instance.persistence.InstancePO;

public interface ContextPlugin {

    void beforeSaving(EntityChange<InstancePO> changes, IInstanceContext context);

    void afterSaving(EntityChange<InstancePO> changes, IInstanceContext context);

    default void postProcess(IInstanceContext context) {}

}
