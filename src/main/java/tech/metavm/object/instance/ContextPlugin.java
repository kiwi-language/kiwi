package tech.metavm.object.instance;

import tech.metavm.entity.EntityChange;
import tech.metavm.entity.InstanceContext;
import tech.metavm.object.instance.persistence.InstancePO;

public interface ContextPlugin {

    void beforeSaving(EntityChange<InstancePO> changes, InstanceContext context);

    void afterSaving(EntityChange<InstancePO> changes, InstanceContext context);

    default void postProcess(InstanceContext context) {}

}
