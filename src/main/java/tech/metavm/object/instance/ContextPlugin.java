package tech.metavm.object.instance;

import tech.metavm.entity.EntityChange;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.persistence.InstancePO;

public interface ContextPlugin {

    boolean beforeSaving(EntityChange<InstancePO> change, IInstanceContext context);

    void afterSaving(EntityChange<InstancePO> change, IInstanceContext context);

    default void postProcess(IInstanceContext context) {}

}
