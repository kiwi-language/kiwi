package tech.metavm.object.instance;

import tech.metavm.entity.EntityChange;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.persistence.VersionRT;
import tech.metavm.object.type.CheckConstraint;
import tech.metavm.util.BusinessException;

import java.util.List;

public class CheckConstraintPlugin implements ContextPlugin {

    @Override
    public boolean beforeSaving(EntityChange<VersionRT> change, IInstanceContext context) {
        change.forEachInsertOrUpdate(v -> {
            var instance = context.get(v.id());
            if(instance instanceof ClassInstance classInstance)
                checkConstraints(classInstance);
        });
        return false;
    }

    private void checkConstraints(ClassInstance instance) {
        List<CheckConstraint> constraints = instance.getKlass().getConstraints(CheckConstraint.class);
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
