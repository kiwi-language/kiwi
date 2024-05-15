package tech.metavm.util;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.persistence.PersistenceUtils;

import java.util.Map;

public class InstanceMatcher extends BaseMatcher<Instance> {

    public static InstanceMatcher of(DurableInstance instance) {
        return new InstanceMatcher(instance);
    }

    private final DurableInstance instance;

    private InstanceMatcher(DurableInstance instance) {
        this.instance = instance;
    }

    @Override
    public boolean matches(Object actual) {
        if(actual instanceof DurableInstance that) {
            Map<Id, DurableInstance> instanceMap = NncUtils.toMap(
                    Instances.getAllNonValueInstances(instance),
                    DurableInstance::getId
            );
            var thatInstances = Instances.getAllNonValueInstances(that);
            if(instanceMap.size() != thatInstances.size()) {
                return false;
            }
            for (var thatInst : thatInstances) {
                var inst = instanceMap.get(thatInst.getId());
                if(inst == null) {
                    return false;
                }
                boolean different = DiffUtils.isPojoDifferent(
                        PersistenceUtils.toInstancePO(inst, TestContext.getAppId()),
                        PersistenceUtils.toInstancePO(thatInst, TestContext.getAppId())
                );
                if(different) {
                    return false;
                }
            }
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(instance.toString());
    }
}
