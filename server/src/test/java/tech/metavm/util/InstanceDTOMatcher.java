package tech.metavm.util;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import tech.metavm.object.instance.rest.InstanceDTO;

import java.util.HashSet;
import java.util.Set;

public class InstanceDTOMatcher extends BaseMatcher<InstanceDTO> {

    public static InstanceDTOMatcher of(InstanceDTO value) {
        return new InstanceDTOMatcher(value, Set.of());
    }

    private final InstanceDTO value;
    private final Set<String> newIds;


    public InstanceDTOMatcher(InstanceDTO value, Set<String> newIds) {
        this.value = value;
        this.newIds = new HashSet<>(newIds);
    }

    @Override
    public boolean matches(Object actual) {
        if(actual instanceof InstanceDTO thatValue)
            return value.valueEquals(thatValue, newIds);
        else
            return false;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(value.toString());
    }
}
