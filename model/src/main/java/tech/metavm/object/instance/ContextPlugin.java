package tech.metavm.object.instance;

import tech.metavm.entity.EntityChange;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.persistence.VersionRT;

public interface ContextPlugin {

    boolean beforeSaving(EntityChange<VersionRT> change, IInstanceContext context);

    void afterSaving(EntityChange<VersionRT> change, IInstanceContext context);

    default void postProcess(IInstanceContext context) {}

}
