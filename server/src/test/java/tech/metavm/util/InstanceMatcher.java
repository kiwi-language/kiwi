package tech.metavm.util;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.persistence.PersistenceUtils;

import java.util.Map;
import java.util.Set;

public class InstanceMatcher extends BaseMatcher<Instance> {

    public static InstanceMatcher of(Instance instance) {
        return new InstanceMatcher(instance);
    }

    private final Instance instance;

    private InstanceMatcher(Instance instance) {
        this.instance = instance;
    }

    @Override
    public boolean matches(Object actual) {
        if(actual instanceof Instance that) {
            Map<Long, Instance> instanceMap = NncUtils.toMap(
                    InstanceUtils.getAllNonValueInstances(instance),
                    Instance::getId
            );
            Set<Instance> thatInstances = InstanceUtils.getAllNonValueInstances(that);
            if(instanceMap.size() != thatInstances.size()) {
                return false;
            }
            for (Instance thatInst : thatInstances) {
                Instance inst = instanceMap.get(thatInst.getId());
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
        description.appendText(instance.toDTO().toString());
    }
}
