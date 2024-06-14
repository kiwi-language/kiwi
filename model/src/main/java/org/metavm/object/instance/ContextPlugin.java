package org.metavm.object.instance;

import org.metavm.entity.EntityChange;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.persistence.VersionRT;

public interface ContextPlugin {

    boolean beforeSaving(EntityChange<VersionRT> change, IInstanceContext context);

    void afterSaving(EntityChange<VersionRT> change, IInstanceContext context);

    default void postProcess(IInstanceContext context) {}

}
