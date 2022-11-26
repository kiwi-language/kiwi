package tech.metavm.object.instance;

import org.springframework.stereotype.Component;
import tech.metavm.entity.EntityChange;
import tech.metavm.entity.InstanceContext;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.meta.CheckConstraintRT;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;

import java.util.List;

@Component
public class CheckConstraintPlugin implements ContextPlugin {

    @Override
    public void beforeSaving(EntityChange<InstancePO> difference, InstanceContext context) {
        List<InstancePO> instancePOs = difference.insertsAndUpdates();
        List<IInstance> instances = NncUtils.map(instancePOs, instancePO -> context.get(instancePO.getId()));
        instances.forEach(this::checkConstraints);
    }

    private void checkConstraints(IInstance instance) {
        List<CheckConstraintRT> constraints = instance.getType().getConstraints(CheckConstraintRT.class);
        for (CheckConstraintRT constraint : constraints) {
            if(!constraint.check(instance)) {
                throw BusinessException.constraintCheckFailed(instance, constraint);
            }
        }
    }

    @Override
    public void afterSaving(EntityChange<InstancePO> difference, InstanceContext context) {

    }

}
