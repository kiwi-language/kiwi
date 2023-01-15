package tech.metavm.object.instance;

import org.springframework.stereotype.Component;
import tech.metavm.entity.EntityChange;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.meta.CheckConstraint;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;

import java.util.List;

@Component
public class CheckConstraintPlugin implements ContextPlugin {

    @Override
    public void beforeSaving(EntityChange<InstancePO> difference, IInstanceContext context) {
        List<InstancePO> instancePOs = difference.insertsAndUpdates();
        List<ClassInstance> instances = NncUtils.mapAndFilterByType(
                instancePOs,
                instancePO -> context.get(instancePO.getId()),
                ClassInstance.class
        );
        instances.forEach(this::checkConstraints);
    }

    private void checkConstraints(ClassInstance instance) {
        List<CheckConstraint> constraints = instance.getType().getConstraints(CheckConstraint.class);
        for (CheckConstraint constraint : constraints) {
            if(!constraint.check(instance)) {
                throw BusinessException.constraintCheckFailed(instance, constraint);
            }
        }
    }

    @Override
    public void afterSaving(EntityChange<InstancePO> difference, IInstanceContext context) {

    }

}
