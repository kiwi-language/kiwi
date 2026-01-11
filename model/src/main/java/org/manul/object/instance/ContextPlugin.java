package org.manul.object.instance;

import org.manul.entity.EntityChange;
import org.manul.object.instance.core.IInstanceContext;
import org.manul.object.instance.core.Patch;
import org.manul.object.instance.persistence.VersionRT;

public interface ContextPlugin {

    boolean beforeSaving(Patch patch, IInstanceContext context);

    void afterSaving(EntityChange<VersionRT> change, IInstanceContext context);

    default void postProcess(IInstanceContext context, Patch patch) {}

}
