package tech.metavm.object.instance;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.metavm.entity.EntityChange;
import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.type.CheckConstraint;
import tech.metavm.util.BusinessException;

import java.util.List;

@Component
@Order(10)
public class CheckConstraintPlugin implements ContextPlugin {

    @Override
    public boolean beforeSaving(EntityChange<InstancePO> change, IInstanceContext context) {
        change.forEachInsertOrUpdate(instancePO -> {
            var instance = context.get(instancePO.getId());
            if(instance instanceof ClassInstance classInstance)
                checkConstraints(classInstance, context.getEntityContext());
        });
        return false;
    }

    private void checkConstraints(ClassInstance instance, IEntityContext entityContext) {
        List<CheckConstraint> constraints = instance.getType().getConstraints(CheckConstraint.class);
        for (CheckConstraint constraint : constraints) {
            if(!constraint.check(instance, entityContext)) {
                throw BusinessException.constraintCheckFailed(instance, constraint);
            }
        }
    }

    @Override
    public void afterSaving(EntityChange<InstancePO> change, IInstanceContext context) {

    }

}
