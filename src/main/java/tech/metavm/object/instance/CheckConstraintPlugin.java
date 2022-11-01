package tech.metavm.object.instance;

import org.springframework.stereotype.Component;
import tech.metavm.object.meta.CheckConstraintRT;
import tech.metavm.util.BusinessException;

import java.util.List;

@Component
public class CheckConstraintPlugin implements ContextPlugin {

    @Override
    public void beforeSaving(ContextDifference difference) {
        List<Instance> instances = difference.getInstancesAfter();
        instances.forEach(this::checkConstraints);
    }

    private void checkConstraints(Instance instance) {
        List<CheckConstraintRT> constraints = instance.getType().getConstraints(CheckConstraintRT.class);
        for (CheckConstraintRT constraint : constraints) {
            if(!constraint.check(instance)) {
                throw BusinessException.constraintCheckFailed(instance, constraint);
            }
        }
    }

    @Override
    public void afterSaving(ContextDifference difference) {

    }
}
