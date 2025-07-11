package org.metavm.object.instance;

import org.metavm.entity.EntityChange;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Patch;
import org.metavm.object.instance.persistence.VersionRT;
import org.metavm.object.type.CheckConstraint;
import org.metavm.util.BusinessException;

import java.util.List;

public class CheckConstraintPlugin implements ContextPlugin {

    @Override
    public boolean beforeSaving(Patch patch, IInstanceContext context) {
        patch.entityChange().forEachInsertOrUpdate(v -> {
            var instance = context.get(v.id());
            if(instance instanceof ClassInstance classInstance)
                checkConstraints(classInstance);
        });
        return false;
    }

    private void checkConstraints(ClassInstance instance) {
        List<CheckConstraint> constraints = instance.getInstanceKlass().getAllConstraints(CheckConstraint.class);
        for (CheckConstraint constraint : constraints) {
            if(!constraint.check(instance)) {
                throw BusinessException.constraintCheckFailed(instance, constraint);
            }
        }
    }

    @Override
    public void afterSaving(EntityChange<VersionRT> change, IInstanceContext context) {

    }

}
