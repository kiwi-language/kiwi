package org.manul.object.instance;

import org.manul.entity.EntityChange;
import org.manul.object.instance.core.ClassInstance;
import org.manul.object.instance.core.IInstanceContext;
import org.manul.object.instance.core.Patch;
import org.manul.object.instance.persistence.VersionRT;
import org.manul.object.type.CheckConstraint;

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
                throw CheckConstraint.constraintCheckFailed(instance, constraint);
            }
        }
    }

    @Override
    public void afterSaving(EntityChange<VersionRT> change, IInstanceContext context) {

    }

}
