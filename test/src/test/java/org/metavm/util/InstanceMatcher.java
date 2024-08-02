package org.metavm.util;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.metavm.object.instance.core.Instance;

import java.util.Arrays;

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
            return Arrays.equals(InstanceOutput.toBytes(instance), InstanceOutput.toBytes(that));
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
